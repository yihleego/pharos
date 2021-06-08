package io.leego.pharos.filter;

import io.leego.pharos.config.PharosProperties;
import io.leego.pharos.constant.PharosHeaderNames;
import io.leego.pharos.context.PharosContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Yihleego
 */
public class PharosGatewayFilter implements GlobalFilter, Ordered {
    protected PharosProperties pharosProperties;

    public PharosGatewayFilter(PharosProperties pharosProperties) {
        this.pharosProperties = pharosProperties;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Obtain the specified scheme from request headers.
        String originalScheme = exchange.getRequest().getHeaders().getFirst(pharosProperties.getSchemeHeaderName());
        exchange.getRequest().mutate().header(PharosHeaderNames.CONTEXT, getContext(originalScheme)).build();
        return chain.filter(exchange);
    }

    public String getContext(String originalScheme) {
        return new PharosContext(
                originalScheme,
                pharosProperties.getDefaultScheme(),
                pharosProperties.getSchemeMetadataName(),
                pharosProperties.getSchemeHeaderName(),
                pharosProperties.getSchemeStrategy().name(),
                originalScheme == null ? null : pharosProperties.getSchemes().get(originalScheme))
                .toString();
    }
}