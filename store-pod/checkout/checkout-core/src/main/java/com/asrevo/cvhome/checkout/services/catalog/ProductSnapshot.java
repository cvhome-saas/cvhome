package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.checkout.entity.OptionLabel;
import com.asrevo.cvhome.commons.domain.Sku;

/**
 * One sku as the catalog and inventory describe it right now: the catalog's cart-line product (name, slug, image,
 * variant labels) merged with inventory's price and purchasability.
 */
public record ProductSnapshot(Sku sku, ReadableCartLineProduct product, BigDecimal finalPrice,
                              BigDecimal originalPrice, boolean discounted, boolean canBePurchased,
                              int quantityOrderMinimum, int quantityOrderMaximum) {

    public Long productId() {
        return product.getProductId();
    }

    public String name() {
        return product.getName() == null ? sku.value() : product.getName();
    }

    public String imageUrl() {
        return product.getImageUrl();
    }

    /** The variant's option/value labels, or nothing for a default variant. */
    public List<OptionLabel> optionLabels() {
        if (product.getVariant() == null || product.getVariant().getOptionValues() == null) {
            return List.of();
        }
        return product.getVariant().getOptionValues().stream()
                .map(value -> new OptionLabel(
                        value.getOptionName() == null ? value.getOptionCode() : value.getOptionName(),
                        value.getValueName() == null ? value.getValueCode() : value.getValueName()))
                .toList();
    }

    public boolean allowsQuantity(int quantity) {
        return quantity >= Math.max(1, quantityOrderMinimum)
                && (quantityOrderMaximum <= 0 || quantity <= quantityOrderMaximum);
    }
}
