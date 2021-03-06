package com.study.wumu.netty4springboot;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * Created by dydy on 2018/1/23.
 */
public class NettyResponseOutputStream extends ServletOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    private WriteListener writeListener;
    private final ChannelHandlerContext ctx;
    private final NettyHttpServletResponse servletResponse;
    private byte[] buf; //缓冲区
    private int count; //缓冲区游标（记录写到哪里）
    private boolean closed; //是否已经调用close()方法关闭输出流

    NettyResponseOutputStream(ChannelHandlerContext ctx, NettyHttpServletResponse servletResponse) {
        this.ctx = ctx;
        this.servletResponse = servletResponse;
        this.buf = new byte[DEFAULT_BUFFER_SIZE];
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

    @Override
    public void write(int b) throws IOException {
        writeBufferIfNeeded(1);
        buf[count++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len > count) {
            flushBuffer();
            ByteBuf content = ctx.alloc().buffer(len);
            content.writeBytes(b, off, len);
            writeContent(content, false);
            return;
        }
        writeBufferIfNeeded(len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    private void writeBufferIfNeeded(int len) throws IOException {
        if (len > buf.length - count) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        flushBuffer(false);
    }

    private void flushBuffer(boolean lastContent) {
        if (count > 0) {
            ByteBuf content = ctx.alloc().buffer(count);
            content.writeBytes(buf, 0, count);
            count = 0;
            writeContent(content, lastContent);
        } else if (lastContent) {
            writeContent(Unpooled.EMPTY_BUFFER, true);
        }
    }

    private void writeContent(ByteBuf content, boolean lastContent) {
        // TODO block if channel is not writable to avoid heap utilisation
        if (!servletResponse.isCommitted()) {
            writeResponse(lastContent);
        }
        if (content.readableBytes() > 0) {
            assert content.refCnt() == 1;
            ctx.write(content, ctx.voidPromise());
        }
        if (lastContent) {
            HttpResponse nettyResponse = servletResponse.getNettyResponse();
            ChannelFuture future = ctx.write(DefaultLastHttpContent.EMPTY_LAST_CONTENT);
            if (!HttpHeaders.isKeepAlive(nettyResponse)) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private void writeResponse(boolean lastContent) {
        HttpResponse response = servletResponse.getNettyResponse();
        // TODO implement exceptions required by http://tools.ietf.org/html/rfc2616#section-4.4
        if (!HttpHeaders.isContentLengthSet(response)) {
            if (lastContent) {
                HttpHeaders.setContentLength(response, count);
            } else {
                HttpHeaders.setTransferEncodingChunked(response);
            }
        }
        ctx.write(response, ctx.voidPromise());
    }

    @Override
    public void close() throws IOException {
        if(closed){
            return;
        }
        closed = true;
        try {
            flushBuffer(true);
            ctx.flush();
        } finally {
            buf = null;
        }
    }
}
