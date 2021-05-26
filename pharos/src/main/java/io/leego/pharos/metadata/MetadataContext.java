package io.leego.pharos.metadata;

/**
 * @author Yihleego
 */
public class MetadataContext {
    private static final ThreadLocal<String> cache = new InheritableThreadLocal<>();

    public static String get() {
        return cache.get();
    }

    public static void set(String metadata) {
        cache.set(metadata);
    }

    public static void remove() {
        cache.remove();
    }
}
