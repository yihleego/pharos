package io.leego.pharos.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.leego.pharos.metadata.MetadataContext;

/**
 * @author Yihleego
 */
public class PharosFeignRequestInterceptor implements RequestInterceptor {
    private final String headerName;

    public PharosFeignRequestInterceptor(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public void apply(RequestTemplate template) {
        String value = MetadataContext.get();
        if (value != null) {
            template.header(headerName, value);
        }
    }
}

