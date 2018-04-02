package com.wacai.wumu.netty4springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Created by dydy on 2018/1/12.
 */
@Slf4j
@Component
public class EmbeddedNettyFactory extends AbstractEmbeddedServletContainerFactory {

    private int DEFAULT_PORT = 8080;

    @Override
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
        NettyContext context = new NettyContext(getContextPath());
        Arrays.stream(initializers).forEach(initializer -> {
            try {
                initializer.onStartup(context);
            } catch (ServletException e) {
                e.printStackTrace();
            }
        });

        // 从spring boot 配置中获取端口，没有则默认8080
        int port = getPort() > 0 ? getPort() : DEFAULT_PORT;
        InetSocketAddress address = new InetSocketAddress(port);
        return new NettyContainer(context, address);
    }

}
