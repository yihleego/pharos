package io.leego.pharos.config;

import io.leego.pharos.enums.LoadBalancerRule;
import io.leego.pharos.loadbalancer.RandomPharosLoadBalancer;
import io.leego.pharos.loadbalancer.RoundRobinPharosLoadBalancer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Yihleego
 */
public class ReactorLoadBalancerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(LoadBalancerClientFactory loadBalancerClientFactory, Environment environment, PharosProperties pharosProperties) {
        String serviceId = loadBalancerClientFactory.getName(environment);
        ObjectProvider<ServiceInstanceListSupplier> provider = loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class);
        LoadBalancerRule rule = pharosProperties.getLoadbalancer().getRule();
        if (rule == LoadBalancerRule.ROUND_ROBIN) {
            return new RoundRobinPharosLoadBalancer(provider, serviceId, pharosProperties);
        } else if (rule == LoadBalancerRule.RANDOM) {
            return new RandomPharosLoadBalancer(provider, serviceId, pharosProperties);
        } else {
            throw new IllegalArgumentException();
        }
    }

}
