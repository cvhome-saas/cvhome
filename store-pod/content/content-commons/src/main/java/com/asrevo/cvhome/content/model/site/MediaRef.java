package com.asrevo.cvhome.content.model.site;

import java.io.Serializable;

/**
 * A media asset as the storefront and console need it: the id to write back, and the resolved URL plus
 * intrinsic dimensions so a caller can render it without a second lookup.
 *
 * @param id     the media asset id, the source of truth
 * @param url    its public URL at the time of the read
 * @param alt    the asset's alt text in the served locale, or {@code null}
 * @param width  intrinsic width in pixels, when known
 * @param height intrinsic height in pixels, when known
 */
public record MediaRef(Long id, String url, String alt, Integer width, Integer height) implements Serializable {
}
