package io.leego.pharos.interceptor;

import io.leego.pharos.metadata.MetadataContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author Yihleego
 */
public class PharosClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private final String headerName;

    public PharosClientHttpRequestInterceptor(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String value = MetadataContext.get();
        if (value != null) {
            request.getHeaders().add(headerName, value);
        }
        return execution.execute(request, body);
    }
}
