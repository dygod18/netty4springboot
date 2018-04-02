package com.wacai.wumu.netty4springboot;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by dydy on 2018/1/17.
 */
@ChannelHandler.Sharable
public class RequestDispatcherHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {

    private final NettyContext context;

    public RequestDispatcherHandler(NettyContext context) {
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest request) throws Exception {
        HttpServletResponse servletResponse = (HttpServletResponse) request.getServletResponse();
        try {
            FilterChain filterChain = context.createFilterChain(request.getRequestURI());
            if (filterChain == null) {
                servletResponse.setStatus(404);
                return;
            }
            filterChain.doFilter(request, servletResponse);
        }  finally {
            if (!request.isAsyncStarted()) {
                servletResponse.getOutputStream().close();
            }
        }

    }

    // 将发送缓冲区中的消息全部写入到socketChannel 中
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        ctx.close();
    }
}
