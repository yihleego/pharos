package io.leego.pharos.config;

import feign.RequestInterceptor;
import io.leego.pharos.interceptor.PharosClientHttpRequestInterceptor;
import io.leego.pharos.interceptor.PharosFeignRequestInterceptor;
import io.leego.pharos.web.PharosHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yihleego
 */
@Configuration
@ComponentScan("io.leego.pharos")
@ConditionalOnProperty(value = "spring.cloud.pharos.enabled", matchIfMissing = true)
@EnableConfigurationProperties(PharosProperties.class)
public class PharosConfiguration {

    @Configuration
    @ConditionalOnClass(ReactorLoadBalancer.class)
    @ConditionalOnProperty(value = "spring.cloud.pharos.loadbalancer.enabled", matchIfMissing = true)
    @LoadBalancerClients(defaultConfiguration = ReactorLoadBalancerConfiguration.class)
    public static class LoadBalancerClientConfiguration {
    }

    @Configuration
    @ConditionalOnClass(RequestInterceptor.class)
    @ConditionalOnProperty(value = "spring.cloud.pharos.feign.enabled", matchIfMissing = true)
    public static class FeignConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public PharosFeignRequestInterceptor pharosFeignRequestInterceptor(PharosProperties pharosProperties) {
            return new PharosFeignRequestInterceptor(pharosProperties.getScheme().getHeaderName());
        }
    }

    @Configuration
    @ConditionalOnClass(ClientHttpRequestInterceptor.class)
    @ConditionalOnProperty(value = "spring.cloud.pharos.rest.enabled", matchIfMissing = true)
    public static class RestConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public PharosClientHttpRequestInterceptor pharosClientHttpRequestInterceptor(PharosProperties pharosProperties) {
            return new PharosClientHttpRequestInterceptor(pharosProperties.getScheme().getHeaderName());
        }
    }

    @Configuration
    @ConditionalOnClass({WebMvcConfigurer.class, HandlerInterceptor.class})
    @ConditionalOnProperty(value = "spring.cloud.pharos.webmvc.enabled", matchIfMissing = true)
    public static class HandlerInterceptorConfiguration implements WebMvcConfigurer {
        @Autowired(required = false)
        private PharosHandlerInterceptor pharosHandlerInterceptor;

        @Bean
        @ConditionalOnMissingBean(PharosHandlerInterceptor.class)
        public PharosHandlerInterceptor pharosHandlerInterceptor(PharosProperties pharosProperties) {
            return new PharosHandlerInterceptor(pharosProperties.getScheme().getHeaderName());
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            if (pharosHandlerInterceptor != null) {
                registry.addInterceptor(pharosHandlerInterceptor)
                        .order(0);
            }
        }
    }

}
