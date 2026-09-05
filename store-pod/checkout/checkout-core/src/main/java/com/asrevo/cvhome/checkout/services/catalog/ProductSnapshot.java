package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;

/**
 * One sku as the catalog and inventory describe it right now: the catalog's product (name, image, variant labels)
 * merged with inventory's price and purchasability.
 */
public record ProductSnapshot(String sku, ReadableMinimalProduct product, BigDecimal finalPrice, BigDecimal originalPrice,
                              boolean discounted, boolean canBePurchased, int quantityOrderMinimum,
                              int quantityOrderMaximum) {

    public Long productId() {
        return product.getId();
    }

    public String name() {
        return product.getDescription() == null || product.getDescription().getName() == null ? sku
                : product.getDescription().getName();
    }

    public String imageUrl() {
        return product.getImage() == null ? null : product.getImage().getImageUrl();
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

    public record OptionLabel(String option, String value) {
    }
}
