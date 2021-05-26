package io.leego.pharos.web;

import io.leego.pharos.metadata.MetadataContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yihleego
 */
public class PharosHandlerInterceptor implements HandlerInterceptor {
    private final String headerName;

    public PharosHandlerInterceptor(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String value = request.getHeader(headerName);
        if (value != null) {
            MetadataContext.set(value);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        MetadataContext.remove();
    }
}
