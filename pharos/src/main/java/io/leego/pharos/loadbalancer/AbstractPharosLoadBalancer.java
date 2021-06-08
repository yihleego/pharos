package io.leego.pharos.loadbalancer;

import io.leego.pharos.constant.PharosHeaderNames;
import io.leego.pharos.context.PharosContext;
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
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Yihleego
 */
public abstract class AbstractPharosLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractPharosLoadBalancer.class);
    protected static final EmptyResponse EMPTY = new EmptyResponse();
    protected final ObjectProvider<ServiceInstanceListSupplier> provider;
    protected final String serviceId;

    /**
     * @param provider  a provider of {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId id of the service for which to choose an instance
     */
    public AbstractPharosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider, String serviceId) {
        this.provider = provider;
        this.serviceId = serviceId;
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
        PharosContext context = PharosContext.parse(headers.getFirst(PharosHeaderNames.CONTEXT));
        String originalScheme = context != null ? context.getOriginalScheme() : null;
        String defaultScheme = context != null ? context.getDefaultScheme() : null;
        String schemeMetadataName = context != null ? context.getSchemeMetadataName() : null;
        Set<String> services = context != null ? context.getServices() : null;
        // Use the default services if the input scheme is empty or not configured.
        if (originalScheme == null || originalScheme.isEmpty()
                || services == null || !services.contains(this.serviceId)) {
            Response<ServiceInstance> defaultResponse = getDefaultResponse(instances, defaultScheme, schemeMetadataName);
            if (!defaultResponse.hasServer()) {
                logger.warn("No default servers available, service-id='{}', scheme='{}'", this.serviceId, originalScheme);
            }
            return defaultResponse;
        }
        // Find the services that match the specified scheme.
        Response<ServiceInstance> specifiedResponse = getSpecifiedResponse(instances, originalScheme, schemeMetadataName);
        if (specifiedResponse.hasServer()) {
            return specifiedResponse;
        }
        // Check the strategy.
        if (!SchemeStrategy.CONSERVATIVE.name().equals(context.getSchemeStrategy())) {
            logger.warn("No specified servers available, service-id='{}', scheme='{}'", this.serviceId, originalScheme);
            return EMPTY;
        }
        // Try to find the default services if the specified services were absent and the specified strategy is conservative.
        Response<ServiceInstance> defaultResponse = getDefaultResponse(instances, defaultScheme, schemeMetadataName);
        if (!defaultResponse.hasServer()) {
            logger.warn("No specified or default servers available, service-id='{}', scheme='{}'", this.serviceId, originalScheme);
        }
        return defaultResponse;
    }

    /**
     * Returns next index.
     * @param bound the upper bound (exclusive). Must be positive.
     */
    public abstract int nextIndex(int bound);

    private Response<ServiceInstance> getDefaultResponse(List<ServiceInstance> instances, String defaultScheme, String schemeMetadataName) {
        return getFilteredResponse(instances, schemeMetadataName, metadataScheme -> metadataScheme == null || metadataScheme.isEmpty() || metadataScheme.equals(defaultScheme));
    }

    private Response<ServiceInstance> getSpecifiedResponse(List<ServiceInstance> instances, String originalScheme, String schemeMetadataName) {
        return getFilteredResponse(instances, schemeMetadataName, originalScheme::equals);
    }

    private Response<ServiceInstance> getFilteredResponse(List<ServiceInstance> instances, String schemeMetadataName, Predicate<String> filter) {
        int size = 0;
        int[] indexes = new int[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            if (filter.test(instances.get(i).getMetadata().get(schemeMetadataName))) {
                indexes[size++] = i;
            }
        }
        if (size == 0) {
            return EMPTY;
        }
        return new DefaultResponse(instances.get(indexes[nextIndex(size)]));
    }
}