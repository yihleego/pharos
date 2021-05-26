package io.leego.pharos.loadbalancer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leego.pharos.config.PharosProperties;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Yihleego
 */
public abstract class AbstractPharosLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractPharosLoadBalancer.class);
    protected static final EmptyResponse EMPTY = new EmptyResponse();
    protected final ObjectMapper objectMapper = new ObjectMapper();
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
        if (supplier instanceof SelectedInstanceCallback && response.hasServer()) {
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
        String metadataName = pharosProperties.getScheme().getMetadataName();
        String headerName = pharosProperties.getScheme().getHeaderName();
        String defaultValue = pharosProperties.getScheme().getDefaultValue();
        String scheme = headers.getFirst(headerName);
        if (scheme == null) {
            // Use default scheme if there is no scheme specified in the request.
            scheme = defaultValue;
        } else {
            String groupHeaderName = pharosProperties.getScheme().getGroupHeaderName();
            Map<String, Set<String>> groups = pharosProperties.getScheme().getGroups();
            if (groups.isEmpty()) {
                groups = parse(headers.getFirst(groupHeaderName));
            } else {
                headers.addIfAbsent(groupHeaderName, format(groups));
            }
            Set<String> specifiedServiceIdSet = groups.get(scheme);
            if (specifiedServiceIdSet == null || !specifiedServiceIdSet.contains(serviceId)) {
                Set<String> defaultServiceIdSet = groups.get(defaultValue);
                if (defaultServiceIdSet == null || !defaultServiceIdSet.contains(serviceId)) {
                    // Return empty response if the service is not configured.
                    return EMPTY;
                }
                // Use default scheme if the service is configured in the default schemes.
                scheme = defaultValue;
            }
        }
        int size = 0;
        int[] indexes = new int[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            String metadata = instances.get(i).getMetadata().getOrDefault(metadataName, defaultValue);
            if (Objects.equals(metadata, scheme)) {
                indexes[size++] = i;
            }
        }
        if (size == 0) {
            logger.warn("No servers available for service '{}' with metadata '{}' = '{}'", serviceId, metadataName, scheme);
            return EMPTY;
        }
        return new DefaultResponse(instances.get(indexes[nextIndex(size)]));
    }

    /**
     * Returns next index.
     * @param bound the upper bound (exclusive). Must be positive.
     */
    public abstract int nextIndex(int bound);

    private String format(Map<String, Set<String>> schemes) {
        if (schemes == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(schemes);
        } catch (JsonProcessingException e) {
            logger.error("Failed to format '{}'", schemes, e);
        }
        return null;
    }

    protected Map<String, Set<String>> parse(String data) {
        if (data == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(data, new TypeReference<Map<String, Set<String>>>() {});
        } catch (IOException e) {
            logger.error("Failed to parse '{}'", data, e);
        }
        return Collections.emptyMap();
    }
}