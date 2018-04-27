package com.study.wumu.netty4springboot;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by dydy on 2018/3/6.
 */
public class SimpleFilterChain implements FilterChain {

    private final Servlet servlet;

    public SimpleFilterChain(Servlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        servlet.service(request, response);
    }
}
