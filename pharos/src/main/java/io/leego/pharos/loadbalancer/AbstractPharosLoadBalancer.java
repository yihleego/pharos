package io.leego.pharos.loadbalancer;

import io.leego.pharos.config.PharosProperties;
import io.leego.pharos.enums.SchemeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yihleego
 */
public abstract class AbstractPharosLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractPharosLoadBalancer.class);
    protected static final EmptyResponse EMPTY = new EmptyResponse();
    protected final ObjectProvider<ServiceInstanceListSupplier> provider;
    protected final String serviceId;
    protected PharosProperties pharosProperties;

    /**
     * @param provider         a provider of {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId        id of the service for which to choose an instance
     * @param pharosProperties the config properties
     */
    public AbstractPharosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider, String serviceId, PharosProperties pharosProperties) {
        this.provider = provider;
        this.serviceId = serviceId;
        this.pharosProperties = pharosProperties;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = provider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(instances -> processInstanceResponse(request, supplier, instances));
    }

    protected Response<ServiceInstance> processInstanceResponse(Request<?> request, ServiceInstanceListSupplier supplier, List<ServiceInstance> instances) {
        Response<ServiceInstance> response = getInstanceResponse(request, instances);
        if (response.hasServer() && supplier instanceof SelectedInstanceCallback) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(response.getServer());
        }
        return response;
    }

    protected Response<ServiceInstance> getInstanceResponse(Request<?> request, List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            logger.warn("No servers available for service '{}'", serviceId);
            return EMPTY;
        }
        if (!(request.getContext() instanceof RequestDataContext)) {
            logger.warn("Request context is not supported");
            return new DefaultResponse(instances.get(nextIndex(instances.size())));
        }
        HttpHeaders headers = ((RequestDataContext) request.getContext()).getClientRequest().getHeaders();
        String defaultScheme = pharosProperties.getDefaultScheme();
        String schemeMetadataName = pharosProperties.getSchemeMetadataName();
        String schemeHeaderName = pharosProperties.getSchemeHeaderName();
        // Obtain the specified scheme from request headers.
        String currentScheme = null;
        String originalScheme = headers.getFirst(schemeHeaderName);
        if (originalScheme != null && !originalScheme.isEmpty()) {
            // Obtain the configured schemes from properties.
            Map<String, Set<String>> schemes = pharosProperties.getSchemes();
            if (schemes != null && !schemes.isEmpty()) {
                Set<String> services = schemes.get(originalScheme);
                if (services != null && services.contains(serviceId)) {
                    // Use the specified scheme when the service is configured.
                    currentScheme = originalScheme;
                }
            }
        }
        int size = 0;
        int[] indexes = new int[instances.size()];
        if (isDefaultScheme(currentScheme, defaultScheme)) {
            for (int i = 0; i < instances.size(); i++) {
                if (isDefaultScheme(instances.get(i).getMetadata().get(schemeMetadataName), defaultScheme)) {
                    indexes[size++] = i;
                }
            }
        } else {
            for (int i = 0; i < instances.size(); i++) {
                if (currentScheme.equals(instances.get(i).getMetadata().get(schemeMetadataName))) {
                    indexes[size++] = i;
                }
            }
            // Try to use default services if the specified services were absent and the conservative strategy is specified.
            if (size == 0 && SchemeStrategy.CONSERVATIVE == pharosProperties.getSchemeStrategy()) {
                for (int i = 0; i < instances.size(); i++) {
                    if (isDefaultScheme(instances.get(i).getMetadata().get(schemeMetadataName), defaultScheme)) {
                        indexes[size++] = i;
                    }
                }
            }
        }
        if (size == 0) {
            logger.warn("No servers available, service-id='{}', scheme='{}'", serviceId, originalScheme);
            return EMPTY;
        }
        return new DefaultResponse(instances.get(indexes[nextIndex(size)]));
    }

    /**
     * Returns next index.
     * @param bound the upper bound (exclusive). Must be positive.
     */
    public abstract int nextIndex(int bound);

    private boolean isDefaultScheme(String currentScheme, String defaultScheme) {
        return currentScheme == null || currentScheme.isEmpty() || currentScheme.equals(defaultScheme);
    }
}