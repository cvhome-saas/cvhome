package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every catalog entity names its store, itself or through its owner, so a write drops only that store's cached
 * reads; anything else clears every store.
 */
class CatalogEntityStoreTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    @Test
    void theEntitiesThatCarryTheStoreNameIt() {
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

        assertThat(CatalogEntityStore.of(product)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(category)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(manufacturer)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(group)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(type)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(option)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(variant)).isEqualTo(STORE);
    }

    @Test
    void thePartsOfAnEntityAnswerThroughTheirOwner() {
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
        ProductOptionValue value = new ProductOptionValue();
        value.setOption(option);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        ProductDescription productDescription = new ProductDescription();
        productDescription.setProduct(product);
        CategoryDescription categoryDescription = new CategoryDescription();
        categoryDescription.setCategory(category);
        ManufacturerDescription manufacturerDescription = new ManufacturerDescription();
        manufacturerDescription.setManufacturer(manufacturer);
        ProductGroupDescription groupDescription = new ProductGroupDescription();
        groupDescription.setProductGroup(group);
        ProductTypeDescription typeDescription = new ProductTypeDescription();
        typeDescription.setProductType(type);
        ProductOptionDescription optionDescription = new ProductOptionDescription();
        optionDescription.setOption(option);
        ProductOptionValueDescription valueDescription = new ProductOptionValueDescription();
        valueDescription.setOptionValue(value);

        assertThat(CatalogEntityStore.of(value)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(image)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(productDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(categoryDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(manufacturerDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(groupDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(typeDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(optionDescription)).isEqualTo(STORE);
        assertThat(CatalogEntityStore.of(valueDescription)).isEqualTo(STORE);
    }

    @Test
    void anythingElseHasNoStoreAndClearsEveryStore() {
        assertThat(CatalogEntityStore.of(null)).isNull();
        assertThat(CatalogEntityStore.of("an outbox row")).isNull();
        assertThat(CatalogEntityStore.of(new ProductImage())).as("an image without its product").isNull();
    }
}
