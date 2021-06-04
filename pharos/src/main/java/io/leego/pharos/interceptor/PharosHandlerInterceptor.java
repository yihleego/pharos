package io.leego.pharos.interceptor;

import io.leego.pharos.config.PharosProperties;
import io.leego.pharos.metadata.MetadataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yihleego
 */
public class PharosHandlerInterceptor implements HandlerInterceptor {
    protected static final Logger logger = LoggerFactory.getLogger(PharosHandlerInterceptor.class);
    private final PharosProperties pharosProperties;
    private final Registration registration;

    public PharosHandlerInterceptor(PharosProperties pharosProperties) {
        this.pharosProperties = pharosProperties;
        this.registration = null;
    }

    public PharosHandlerInterceptor(PharosProperties pharosProperties, Registration registration) {
        this.pharosProperties = pharosProperties;
        this.registration = registration;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String value = request.getHeader(pharosProperties.getSchemeHeaderName());
        if (logger.isDebugEnabled() && registration != null) {
            logger.debug("Intercepting request \033[32m{} {}\033[0m {service-id='{}', instance-id='{}', metadata='{}', header='{}'}",
                    request.getMethod(),
                    request.getRequestURI(),
                    registration.getServiceId(),
                    registration.getInstanceId(),
                    registration.getMetadata().get(pharosProperties.getSchemeMetadataName()),
                    value);
        }
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
