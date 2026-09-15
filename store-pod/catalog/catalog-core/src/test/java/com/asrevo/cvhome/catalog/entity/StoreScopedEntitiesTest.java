package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every catalog entity names its store, a part through its owner, so a committed write to any of them drops the
 * right store's cached reads and never another's; an orphaned part names none.
 */
class StoreScopedEntitiesTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    @Test
    void ownersNameTheirStoreDirectly() {
        Product product = new Product();
        product.setStore(STORE);
        Category category = new Category();
        category.setStoreMerchantId(STORE);
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setStoreMerchantId(STORE);
        ProductGroup group = new ProductGroup();
        group.setStoreMerchantId(STORE);
        ProductType type = new ProductType();
        type.setStoreMerchantId(STORE);
        ProductOption option = new ProductOption();
        option.setStoreMerchantId(STORE);
        ProductVariant variant = new ProductVariant();
        variant.setStoreMerchantId(STORE);
        ProductSearchIndex index = new ProductSearchIndex();
        index.setStore(STORE);

        for (StoreScoped owner : new StoreScoped[] {product, category, manufacturer, group, type, option, variant, index}) {
            assertThat(owner.scopedStore()).as(owner.getClass().getSimpleName()).isEqualTo(STORE);
        }
    }

    @Test
    void partsAnswerThroughTheirOwnerAndAnOrphanNamesNone() {
        Product product = new Product();
        product.setStore(STORE);
        ProductOption option = new ProductOption();
        option.setStoreMerchantId(STORE);
        ProductOptionValue value = new ProductOptionValue(option);
        ProductVariant variant = new ProductVariant();
        variant.setStoreMerchantId(STORE);
        Category category = new Category();
        category.setStoreMerchantId(STORE);
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setStoreMerchantId(STORE);
        ProductGroup group = new ProductGroup();
        group.setStoreMerchantId(STORE);
        ProductType type = new ProductType();
        type.setStoreMerchantId(STORE);
        ProductVariantOptionValue variantValue = new ProductVariantOptionValue();
        variantValue.setVariant(variant);
        ProductOptionAssignment assignment = new ProductOptionAssignment();
        assignment.setProduct(product);

        StoreScoped[] parts = {new ProductDescription(product), new ProductImage(), value, new CategoryDescription(category),
            new ManufacturerDescription(manufacturer), new ProductGroupDescription(group), new ProductTypeDescription(type),
            new ProductOptionDescription(option), new ProductOptionValueDescription(value), variantValue, assignment};
        ProductImage image = (ProductImage) parts[1];
        image.setProduct(product);

        for (StoreScoped part : parts) {
            assertThat(part.scopedStore()).as(part.getClass().getSimpleName()).isEqualTo(STORE);
        }
        StoreScoped[] orphans = {new ProductDescription(), new ProductImage(), new ProductOptionValue(),
            new CategoryDescription(), new ManufacturerDescription(), new ProductGroupDescription(),
            new ProductTypeDescription(), new ProductOptionDescription(), new ProductOptionValueDescription(),
            new ProductVariantOptionValue(), new ProductOptionAssignment()};
        for (StoreScoped orphan : orphans) {
            assertThat(orphan.scopedStore()).as(orphan.getClass().getSimpleName()).isNull();
        }
    }
}
