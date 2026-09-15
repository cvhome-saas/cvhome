package com.asrevo.cvhome.cache;

/**
 * A region's counters since the service started; what the meters read.
 *
 * @param hits      reads answered from the region
 * @param misses    reads that went to the loader
 * @param puts      values stored, by a loader or a caller
 * @param evictions entries the provider dropped for size or age; a store's version bump is not counted
 * @param size      entries held now, as far as the provider can say
 */
public record RegionStats(long hits, long misses, long puts, long evictions, long size) {

    public static RegionStats none() {
        return new RegionStats(0, 0, 0, 0, 0);
    }
}
