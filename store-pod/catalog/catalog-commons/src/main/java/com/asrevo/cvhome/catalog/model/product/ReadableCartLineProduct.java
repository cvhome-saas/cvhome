package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Sku;

import lombok.Getter;
import lombok.Setter;

/**
 * What a cart or order line needs to know about its sku, and nothing else: the product it belongs to, its name and
 * slug in one language, one image, whether the catalogue still offers it, and the option labels of a combination
 * variant. Price and stock are inventory's and are read beside it.
 *
 * <p>
 * The full {@link ReadableMinimalProduct} carried the description's copy, every image, the dimensions and their
 * units for each line, on every cart read: in the 2026-09-14 load test that read was catalog's largest cost. This is
 * a kilobyte where that was tens, and catalog answers it from a per-sku cache.
 * </p>
 */
@Getter
@Setter
public class ReadableCartLineProduct implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Sku sku;

    private Long productId;

    /** The name in the language asked for, or in the product's first language when it has none in that one. */
    private String name;

    /** The storefront slug in the same language, for the line's link to its product page. */
    private String friendlyUrl;

    /** The default image's URL, or null for a product without one. */
    private String imageUrl;

    /** Whether the catalogue still offers the product; stock is inventory's answer. */
    private boolean available;

    /** The option/value labels of a combination variant; null for a default variant. */
    private ReadableVariantSelection variant;
}
