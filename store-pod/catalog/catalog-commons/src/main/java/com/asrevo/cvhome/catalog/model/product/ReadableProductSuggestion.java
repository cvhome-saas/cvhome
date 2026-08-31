package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of the autocomplete dropdown.
 *
 * <p>
 * Deliberately smaller than {@link ReadableProduct}: this is answered on every keystroke, so it carries only what
 * a dropdown row draws and what a click needs to navigate. Anything more would be paid for by the shopper's
 * latency and never rendered.
 * </p>
 */
@Getter
@Setter
public class ReadableProductSuggestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    /**
     * The slug of the product page, in the language asked for.
     */
    private String friendlyUrl;

    /**
     * The default variant's sku.
     */
    private String sku;

    /**
     * Set when the typed query matched one concrete variant sku — the storefront deep-links the product page
     * with it ({@code ?sku=}) so the shopper lands preselected. Null otherwise.
     */
    private String matchedVariantSku;

    private String imageUrl;

    private String brand;
}
