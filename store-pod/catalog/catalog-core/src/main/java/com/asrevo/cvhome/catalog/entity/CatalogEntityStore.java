package com.asrevo.cvhome.catalog.entity;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Which store a catalog entity belongs to, for the cache eviction that follows its write: the entities that carry
 * the store say so, and a description, image, option value or variant answers through its owner. Anything else is
 * null, which clears every store's cache rather than risk a stale one.
 */
public final class CatalogEntityStore {

    private CatalogEntityStore() {
    }

    public static StoreMerchantId of(Object entity) {
        return switch (entity) {
            case Product product -> product.getStore();
            case Category category -> category.getStoreMerchantId();
            case Manufacturer manufacturer -> manufacturer.getStoreMerchantId();
            case ProductGroup group -> group.getStoreMerchantId();
            case ProductType type -> type.getStoreMerchantId();
            case ProductOption option -> option.getStoreMerchantId();
            case ProductVariant variant -> variant.getStoreMerchantId();
            case null, default -> ofPart(entity);
        };
    }

    /** The rows that belong to one of the above: an option's value, a product's image, and every description. */
    private static StoreMerchantId ofPart(Object entity) {
        return switch (entity) {
            case ProductOptionValue value -> of(value.getOption());
            case ProductImage image -> of(image.getProduct());
            case null, default -> ofDescription(entity);
        };
    }

    private static StoreMerchantId ofDescription(Object entity) {
        return switch (entity) {
            case ProductDescription description -> of(description.getProduct());
            case CategoryDescription description -> of(description.getCategory());
            case ManufacturerDescription description -> of(description.getManufacturer());
            case ProductGroupDescription description -> of(description.getProductGroup());
            case ProductTypeDescription description -> of(description.getProductType());
            case ProductOptionDescription description -> of(description.getOption());
            case ProductOptionValueDescription description -> of(description.getOptionValue());
            case null, default -> null;
        };
    }
}
