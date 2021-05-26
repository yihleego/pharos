package io.leego.pharos.config;

import io.leego.pharos.enums.LoadBalancerRule;
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
    @NestedConfigurationProperty
    private Scheme scheme = new Scheme();
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

    public Scheme getScheme() {
        return scheme;
    }

    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
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

    public static class Scheme {
        private String defaultValue = "default";
        private String metadataName = "access_scheme";
        private String headerName = "Access-Scheme";
        private String groupHeaderName = "Scheme-Group";
        /** key: scheme, value: service-id set */
        private Map<String, Set<String>> groups = Collections.emptyMap();

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getMetadataName() {
            return metadataName;
        }

        public void setMetadataName(String metadataName) {
            this.metadataName = metadataName;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getGroupHeaderName() {
            return groupHeaderName;
        }

        public void setGroupHeaderName(String groupHeaderName) {
            this.groupHeaderName = groupHeaderName;
        }

        public Map<String, Set<String>> getGroups() {
            return groups;
        }

        public void setGroups(Map<String, Set<String>> groups) {
            this.groups = groups;
        }
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
