package com.asrevo.cvhome.cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The regions one service declares: its one {@code CacheRegions} bean, built from its region enum.
 *
 * @param regions the declared regions, in declaration order; names are unique
 */
public record CacheRegions(List<CacheRegion> regions) {

    public CacheRegions {
        regions = List.copyOf(regions);
        Map<String, CacheRegion> seen = new LinkedHashMap<>();
        for (CacheRegion region : regions) {
            if (seen.put(region.regionName(), region) != null) {
                throw new IllegalArgumentException(String.format("cache region declared twice: %s", region.regionName()));
            }
        }
    }

    public static CacheRegions of(CacheRegion... regions) {
        return new CacheRegions(List.of(regions));
    }

    public static CacheRegions of(Collection<? extends CacheRegion> regions) {
        return new CacheRegions(List.copyOf(regions));
    }

    public static CacheRegions none() {
        return new CacheRegions(List.of());
    }

    public Optional<CacheRegion> byName(String name) {
        return regions.stream().filter(region -> region.regionName().equals(name)).findFirst();
    }

    public List<String> names() {
        return regions.stream().map(CacheRegion::regionName).toList();
    }
}
