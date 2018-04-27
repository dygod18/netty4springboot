package com.study.wumu.netty4springboot;

import com.google.common.base.StandardSystemProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by dydy on 2018/1/12.
 */
public class NettyContainer implements EmbeddedServletContainer {

    private final NettyContext context;
    private final InetSocketAddress address;

    // Netty 所需的线程池，分别用于接收/监听请求以及处理请求读写
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup servletExecutor;

    public NettyContainer(NettyContext context, InetSocketAddress address) {
        this.context = context;
        this.address = address;
    }

    @Override
    public void start() throws EmbeddedServletContainerException {
        context.setInitialized(false);

        ServerBootstrap sb = new ServerBootstrap();
        // 根据不同系统初始化对应的 EventLoopGroup
        if ("Linux".equals(StandardSystemProperty.OS_NAME.value())) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            sb.channel(NioServerSocketChannel.class)
                    .group(bossGroup, workerGroup)
                    .option(EpollChannelOption.TCP_CORK, true);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            sb.channel(NioServerSocketChannel.class)
                    .group(bossGroup, workerGroup);
        }

        sb.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024);

        servletExecutor = new DefaultEventExecutorGroup(2);
        sb.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast("codec", new HttpServerCodec(4096, 8192, 8192, false));
                p.addLast("servletInput", new ServletContentHandler(context));
                p.addLast(checkNotNull(servletExecutor), "filterChain", new RequestDispatcherHandler(context));
            }
        });

        context.setInitialized(true);

        ChannelFuture future = sb.bind(address).awaitUninterruptibly();
        Throwable cause = future.cause();
        if (null != cause) {
            throw new EmbeddedServletContainerException("Could not start Netty server", cause);
        }
    }

    @Override
    public void stop() throws EmbeddedServletContainerException {
        try {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
            if(null != servletExecutor) {
                servletExecutor.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPort() {
        return address.getPort();
    }
}
