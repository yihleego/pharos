package io.leego.pharos.config;

import io.leego.pharos.enums.LoadBalancerRule;
import io.leego.pharos.enums.SchemeStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Yihleego
 */
@ConfigurationProperties("spring.cloud.pharos")
public class PharosProperties {
    private boolean enabled = true;
    /** key: scheme, value: service-id set */
    private Map<String, Set<String>> schemes = Collections.emptyMap();
    private String defaultScheme;
    private String schemeMetadataName = "pharos_scheme";
    private String schemeHeaderName = "Pharos-Scheme";
    private SchemeStrategy schemeStrategy = SchemeStrategy.CONSERVATIVE;
    @NestedConfigurationProperty
    private Loadbalancer loadbalancer = new Loadbalancer();
    @NestedConfigurationProperty
    private Feign feign = new Feign();
    @NestedConfigurationProperty
    private Rest rest = new Rest();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Set<String>> getSchemes() {
        return schemes;
    }

    public void setSchemes(Map<String, Set<String>> schemes) {
        this.schemes = schemes;
    }

    public String getDefaultScheme() {
        return defaultScheme;
    }

    public void setDefaultScheme(String defaultScheme) {
        this.defaultScheme = defaultScheme;
    }

    public String getSchemeMetadataName() {
        return schemeMetadataName;
    }

    public void setSchemeMetadataName(String schemeMetadataName) {
        this.schemeMetadataName = schemeMetadataName;
    }

    public String getSchemeHeaderName() {
        return schemeHeaderName;
    }

    public void setSchemeHeaderName(String schemeHeaderName) {
        this.schemeHeaderName = schemeHeaderName;
    }

    public SchemeStrategy getSchemeStrategy() {
        return schemeStrategy;
    }

    public void setSchemeStrategy(SchemeStrategy schemeStrategy) {
        this.schemeStrategy = schemeStrategy;
    }

    public Loadbalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(Loadbalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public Feign getFeign() {
        return feign;
    }

    public void setFeign(Feign feign) {
        this.feign = feign;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(Rest rest) {
        this.rest = rest;
    }

    public static class Loadbalancer {
        private boolean enabled = true;
        private LoadBalancerRule rule = LoadBalancerRule.ROUND_ROBIN;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public LoadBalancerRule getRule() {
            return rule;
        }

        public void setRule(LoadBalancerRule rule) {
            this.rule = rule;
        }
    }

    public static class Feign {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Rest {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
