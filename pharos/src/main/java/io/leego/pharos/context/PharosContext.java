package io.leego.pharos.context;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yihleego
 */
public class PharosContext implements Serializable {
    private String originalScheme;
    private String defaultScheme;
    private String schemeMetadataName;
    private String schemeHeaderName;
    private String schemeStrategy;
    private Set<String> services;

    public PharosContext() {
    }

    public PharosContext(String originalScheme, String defaultScheme, String schemeMetadataName, String schemeHeaderName, String schemeStrategy, Set<String> services) {
        this.originalScheme = originalScheme;
        this.defaultScheme = defaultScheme;
        this.schemeMetadataName = schemeMetadataName;
        this.schemeHeaderName = schemeHeaderName;
        this.schemeStrategy = schemeStrategy;
        this.services = services;
    }

    public String getOriginalScheme() {
        return originalScheme;
    }

    public void setOriginalScheme(String originalScheme) {
        this.originalScheme = originalScheme;
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

    public String getSchemeStrategy() {
        return schemeStrategy;
    }

    public void setSchemeStrategy(String schemeStrategy) {
        this.schemeStrategy = schemeStrategy;
    }

    public Set<String> getServices() {
        return services;
    }

    public void setServices(Set<String> services) {
        this.services = services;
    }

    public static PharosContext parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] array = value.split(",", 6);
        PharosContext pharosContext = new PharosContext();
        pharosContext.setOriginalScheme(trim(array[0]));
        pharosContext.setDefaultScheme(trim(array[1]));
        pharosContext.setSchemeMetadataName(trim(array[2]));
        pharosContext.setSchemeHeaderName(trim(array[3]));
        pharosContext.setSchemeStrategy(trim(array[4]));
        if (array[5] != null && !array[5].isEmpty()) {
            pharosContext.setServices(Arrays.stream(array[5].split("\\|")).collect(Collectors.toSet()));
        }
        return pharosContext;
    }

    @Override
    public String toString() {
        return (originalScheme != null ? originalScheme : "") + ',' +
                (defaultScheme != null ? defaultScheme : "") + ',' +
                (schemeMetadataName != null ? schemeMetadataName : "") + ',' +
                (schemeHeaderName != null ? schemeHeaderName : "") + ',' +
                (schemeStrategy != null ? schemeStrategy : "") + ',' +
                (services != null && !services.isEmpty() ? services.stream().map(String::trim).collect(Collectors.joining("|")) : "");
    }

    private static String trim(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s.trim();
    }
}
