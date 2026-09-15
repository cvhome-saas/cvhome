package com.asrevo.cvhome.cache.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The configuration of the cache regions: which provider by default, and per region what differs from the code's
 * declaration. Read at run time by the registry, never as a bean condition.
 *
 * <p>
 * A region {@code catalog.product} is configured as {@code regions.catalog.product}: the service, then the read,
 * so the YAML nests the way the name does and a dot never has to be escaped in a key.
 * </p>
 *
 * @param defaultProvider the provider of every region that names none: {@code caffeine}
 * @param regions         overrides by service, then by read
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.cache")
public record CacheProperties(@DefaultValue("caffeine") String defaultProvider,
                              @DefaultValue Map<String, Map<String, Region>> regions) {

    public static CacheProperties defaults() {
        return new CacheProperties("caffeine", Map.of());
    }

    /** The override of the region named {@code <service>.<read>}, or one that changes nothing. */
    public Region region(String name) {
        int dot = name.indexOf('.');
        if (regions == null || dot <= 0) {
            return Region.NONE;
        }
        Map<String, Region> service = regions.get(name.substring(0, dot));
        Region region = service == null ? null : service.get(name.substring(dot + 1));
        return region == null ? Region.NONE : region;
    }

    /** Every configured region's name, {@code <service>.<read>}. */
    public List<String> configuredNames() {
        if (regions == null) {
            return List.of();
        }
        return regions.entrySet().stream()
                .flatMap(service -> service.getValue().keySet().stream().map(read -> String.format("%s.%s", service.getKey(), read)))
                .toList();
    }

    /**
     * What configuration may change about one region; a field left out keeps the code's value.
     *
     * @param provider the provider's name, or {@code null} for the default
     * @param ttl      the time-to-live, or {@code null} for the declared one
     * @param maxSize  the most entries kept, or {@code null} for the declared bound
     * @param enabled  {@code false} switches the region off: every read misses, the meters still count
     */
    public record Region(String provider, Duration ttl, Long maxSize, @DefaultValue("true") Boolean enabled) {

        static final Region NONE = new Region(null, null, null, Boolean.TRUE);

        public boolean isEnabled() {
            return enabled == null || enabled;
        }
    }
}
