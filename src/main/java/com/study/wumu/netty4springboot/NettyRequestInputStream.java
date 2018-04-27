package com.study.wumu.netty4springboot;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dydy on 2018/1/17.
 */
public class NettyRequestInputStream extends ServletInputStream {

    private final Channel channel;
    private final BlockingQueue<HttpContent> queue;
    private AtomicBoolean closed;

    public NettyRequestInputStream(Channel channel) {
        this.channel = channel;
        this.closed = new AtomicBoolean();
        queue = new LinkedBlockingDeque<>();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    public void addContent(HttpContent content) {
        queue.offer(content);
    }
}
