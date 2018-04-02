package com.wacai.wumu.netty4springboot;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

/**
 * Created by dydy on 2018/1/16.
 */
public class ServletContentHandler extends ChannelInboundHandlerAdapter {

    private NettyContext servletContext;
    private NettyRequestInputStream inputstream;

    ServletContentHandler(NettyContext nettyContext) {
        this.servletContext = nettyContext;
    }

    public NettyContext getServletContext() {
        return servletContext;
    }

    public NettyRequestInputStream getInputstream(){
        return inputstream;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        inputstream = new NettyRequestInputStream(ctx.channel());
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
            HttpUtil.setKeepAlive(response, HttpUtil.isKeepAlive(request));
            NettyHttpServletResponse servletResponse = new NettyHttpServletResponse(ctx, servletContext, response);
            NettyHttpServletRequest servletRequest = new NettyHttpServletRequest(ctx, this, request, servletResponse);
            servletResponse.setRequest(servletRequest);
            if(HttpUtil.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE), ctx.voidPromise());
            }
            ctx.fireChannelRead(servletRequest);
        }
    }

}
