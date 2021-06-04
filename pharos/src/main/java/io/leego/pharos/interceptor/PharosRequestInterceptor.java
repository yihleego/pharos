package io.leego.pharos.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.leego.pharos.config.PharosProperties;
import io.leego.pharos.metadata.MetadataContext;

/**
 * @author Yihleego
 */
public class PharosRequestInterceptor implements RequestInterceptor {
    private final PharosProperties pharosProperties;

    public PharosRequestInterceptor(PharosProperties pharosProperties) {
        this.pharosProperties = pharosProperties;
    }

    @Override
    public void apply(RequestTemplate template) {
        String value = MetadataContext.get();
        if (value != null) {
            template.header(pharosProperties.getSchemeHeaderName(), value);
        }
    }
}

