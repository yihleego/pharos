package io.leego.pharos.loadbalancer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yihleego
 */
public class RoundRobinPharosLoadBalancer extends AbstractPharosLoadBalancer {
    protected final AtomicInteger counter;

    /**
     * @param provider  a provider of {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId id of the service for which to choose an instance
     */
    public RoundRobinPharosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider, String serviceId) {
        super(provider, serviceId);
        this.counter = new AtomicInteger(new Random().nextInt(1000));
    }

    /**
     * @param provider  a provider of {@link ServiceInstanceListSupplier} that will be used to get available instances
     * @param serviceId id of the service for which to choose an instance
     * @param seed      the round robin element position marker
     */
    public RoundRobinPharosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> provider, String serviceId, int seed) {
        super(provider, serviceId);
        this.counter = new AtomicInteger(seed);
    }

    @Override
    public int nextIndex(int bound) {
        return Math.abs(this.counter.incrementAndGet()) % bound;
    }
}