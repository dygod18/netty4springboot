package com.study.wumu.netty4springboot;

import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;

/**
 * Created by dydy on 2018/1/17.
 */
public class NettyHttpServletResponse implements HttpServletResponse {

    private NettyHttpServletRequest request;
    private final NettyContext servletContext;
    private final HttpResponse httpResponse;
    private NettyResponseOutputStream outputStream;
    private boolean usingOutputStream;
    private PrintWriter writer;
    private boolean committed;
    private String contentType;

    public NettyHttpServletResponse(ChannelHandlerContext ctx, NettyContext servletContext, HttpResponse response) {
        this.servletContext = servletContext;
        this.httpResponse = response;
        this.outputStream = new NettyResponseOutputStream(ctx, this);
    }

    public HttpResponse getNettyResponse() {
        if (committed) {
            return httpResponse;
        }
        committed = true;
        // TODO cookies
        return httpResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        System.out.println(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        httpResponse.setStatus(new HttpResponseStatus(sc, msg));
    }

    @Override
    public void sendError(int sc) throws IOException {
        httpResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {
    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {
        if(setHeaderField(name, value)) {
            return;
        }
        httpResponse.headers().set(name, value);
    }

    private boolean setHeaderField(String name, String value) {
        // Handle headers that shouldn't be set directly, and are instance fields
        char c = name.charAt(0);
        if ('C' == c || 'c' == c) {
            if (HttpHeaders.Names.CONTENT_TYPE.equalsIgnoreCase(name)) {
                setContentType(value);
                return true;
            }
        }
        return false;
    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {
        httpResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void setStatus(int sc, String sm) {
        httpResponse.setStatus(new HttpResponseStatus(sc, sm));
    }

    @Override
    public int getStatus() {
        return httpResponse.status().code();
    }

    @Override
    public String getHeader(String name) {
        return httpResponse.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return httpResponse.headers().getAll(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return httpResponse.headers().names();
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        usingOutputStream = true;
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(null == writer) {
            writer = new PrintWriter(outputStream);
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {
        MediaType mediaType = MediaType.parse(type);
        Optional<Charset> charset = mediaType.charset();
        if (charset.isPresent()) {
            setCharacterEncoding(charset.get().name());
        }
        contentType = mediaType.type() + '/' + mediaType.subtype();
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public NettyHttpServletResponse setRequest(NettyHttpServletRequest request) {
        this.request = request;
        return this;
    }
}
