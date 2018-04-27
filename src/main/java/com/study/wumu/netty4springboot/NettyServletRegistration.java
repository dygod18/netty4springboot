package com.study.wumu.netty4springboot;

import javax.servlet.*;
import java.util.*;

/**
 * Created by dydy on 2018/1/12.
 */
public class NettyServletRegistration implements ServletRegistration.Dynamic, Registration, Registration.Dynamic, ServletConfig, FilterConfig {

    private Servlet servlet;
    private volatile boolean initialised;
    private final String name;
    private final String className;
    private final NettyContext context;

    public Servlet getServlet() throws ServletException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {
                    if (null == servlet) {
                        try {
                            servlet = (Servlet) Class.forName(getClassName()).newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    servlet.init(this);
                    initialised = true;
                }
            }
        }
        return servlet;
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        NettyContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addServletMapping(urlPattern, getName());
        }
        return Collections.emptySet();
    }

    NettyServletRegistration(NettyContext context, String servletName, String className, Servlet servlet) {
        this.servlet = servlet;
        this.name = servletName;
        this.className = className;
        this.context = context;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    protected NettyContext getNettyContext() {
        return context;
    }

    @Override
    public Collection<String> getMappings() {
        return null;
    }

    @Override
    public String getRunAsRole() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public String getFilterName() {
        return name;
    }

    @Override
    public String getServletName() {
        return name;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return Collections.emptyMap();
    }
}
