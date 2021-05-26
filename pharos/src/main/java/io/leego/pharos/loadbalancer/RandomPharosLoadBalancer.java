package io.leego.pharos.loadbalancer;

import io.leego.pharos.config.PharosProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Yihleego
 */
public class RandomPharosLoadBalancer extends AbstractPharosLoadBalancer {

    /**
     * @param provider         a provider of {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId        id of the service for which to choose an instance
     * @param pharosProperties the config properties
     */
    public RandomPharosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider, String serviceId, PharosProperties pharosProperties) {
        super(provider, serviceId, pharosProperties);
    }

    @Override
    public int nextIndex(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}