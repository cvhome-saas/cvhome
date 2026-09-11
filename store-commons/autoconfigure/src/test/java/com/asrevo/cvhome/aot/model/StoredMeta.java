package com.asrevo.cvhome.aot.model;

/**
 * Stands in for content's {@code BannerMeta}: a record kept as a JSON column, which no controller names.
 *
 * @param target where the banner leads
 * @param loggedInOnly whether only a signed-in shopper sees it
 */
public record StoredMeta(String target, boolean loggedInOnly) {
}
