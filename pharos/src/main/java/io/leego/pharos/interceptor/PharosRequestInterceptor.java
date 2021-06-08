package io.leego.pharos.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.leego.pharos.constant.PharosHeaderNames;
import io.leego.pharos.metadata.MetadataContext;

/**
 * @author Yihleego
 */
public class PharosRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String value = MetadataContext.get();
        if (value != null) {
            template.header(PharosHeaderNames.CONTEXT, value);
        }
    }

}

