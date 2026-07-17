package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.catalog.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.model.references.WeightUnitOfMeasure;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableProductDefinitionMapper implements Mapper<Product, ReadableProductDefinition> {

    private final ReadableCategoryMapper readableCategoryMapper;

    private final ReadableProductTypeMapper readableProductTypeMapper;

    private final ReadableManufacturerMapper readableManufacturerMapper;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final ImageFilePath imageUtils;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableProductDefinitionMapper(ReadableCategoryMapper readableCategoryMapper,
                                           ReadableProductTypeMapper readableProductTypeMapper,
                                           ReadableManufacturerMapper readableManufacturerMapper,
                                           ReadableInventoryMapper readableInventoryMapper, ImageFilePath imageUtils,
                                           ExternalMerchantStoreService externalMerchantStoreService) {
        this.readableCategoryMapper = readableCategoryMapper;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.readableManufacturerMapper = readableManufacturerMapper;
        this.readableInventoryMapper = readableInventoryMapper;
        this.imageUtils = imageUtils;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableProductDefinition convert(Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProductDefinition target = new ReadableProductDefinition();
        return this.merge(source, target, store, language);
    }

    @Override
    public ReadableProductDefinition merge(Product source, ReadableProductDefinition target, StoreMerchantId store,
                                           LanguageCode language) {
        target.setIdentifier(source.getSku());
        target.setId(source.getId());
        target.setVisible(source.isAvailable());
        target.setDateAvailable(source.getDateAvailable());
        target.setSku(source.getSku());

        if (LanguageCode.isAllLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            target.setDescriptions(descriptionSet.stream().map(this::populateDescription).toList());
        }
        if (LanguageCode.isLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
            var description = descriptionSet.stream()
                    .filter(it -> language.equals(it.getLanguageCode()))
                    .findFirst()
                    .map(this::populateDescription)
                    .orElse(null);
            target.setDescription(description);
        }

        if (source.getManufacturer() != null) {
            ReadableManufacturer manufacturer = readableManufacturerMapper.convert(source.getManufacturer(), store,
                    language);
            target.setManufacturer(manufacturer);
        }

        if (!CollectionUtils.isEmpty(source.getCategories())) {
            List<ReadableCategory> categoryList = new ArrayList<>();
            for (Category category : source.getCategories()) {
                ReadableCategory readableCategory = readableCategoryMapper.convert(category, store, language);
                categoryList.add(readableCategory);
            }
            target.setCategories(categoryList);
        }

        ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);
        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        specifications
                .setDimensionUnitOfMeasure(DimensionUnitOfMeasure.valueOf(baseStore.getDimension().name().toLowerCase()));
        specifications.setWeightUnitOfMeasure(WeightUnitOfMeasure.valueOf(baseStore.getWeight().name().toLowerCase()));

        target.setProductSpecifications(specifications);

        if (source.getType() != null) {
            ReadableProductType readableType = readableProductTypeMapper.convert(source.getType(), store, language);
            target.setType(readableType);
        }

        target.setSortOrder(source.getSortOrder());

        // images
        Set<ProductImage> images = source.getImages();
        if (CollectionUtils.isNotEmpty(images)) {

            List<ReadableImage> imageList = images.stream()
                    .map(i -> this.convertImage(source, i, store))
                    .toList();
            target.setImages(imageList);
        }

        // quantity
        ProductAvailability availability = null;
        for (ProductAvailability a : source.getAvailabilities()) {
            availability = a;
        }

        if (availability != null) {
            target.setCanBePurchased(availability.isProductStatus());
            ReadableInventory inventory = readableInventoryMapper.convert(availability, store, language);
            target.setInventory(inventory);
        }

        return target;
    }

    private ReadableImage convertImage(Product product, ProductImage image, StoreMerchantId store) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(image.getProductImage());
        prdImage.setDefaultImage(image.isDefaultImage());

        StringBuilder imgPath = new StringBuilder();
        imgPath.append(imageUtils.getContextPath())
                .append(imageUtils.buildProductImageUtils(store, product.getSku(), image.getProductImage()));

        prdImage.setImageUrl(imgPath.toString());
        prdImage.setId(image.getId());
        prdImage.setImageType(image.getImageType());
        if (image.getProductImageUrl() != null) {
            prdImage.setExternalUrl(image.getProductImageUrl());
        }
        if (image.getImageType() == 1 && image.getProductImageUrl() != null) { // video
            prdImage.setVideoUrl(image.getProductImageUrl());
        }

        if (prdImage.isDefaultImage()) {
            prdImage.setDefaultImage(true);
        }

        return prdImage;
    }

    private com.asrevo.cvhome.catalog.model.product.ProductDescription populateDescription(
            ProductDescription description) {
        if (description == null) {
            return null;
        }

        com.asrevo.cvhome.catalog.model.product.ProductDescription tragetDescription =
                new com.asrevo.cvhome.catalog.model.product.ProductDescription();
        tragetDescription.setFriendlyUrl(description.getSeUrl());
        tragetDescription.setName(description.getName());
        tragetDescription.setId(description.getId());
        if (!StringUtils.isBlank(description.getMetatagTitle())) {
            tragetDescription.setTitle(description.getMetatagTitle());
        } else {
            tragetDescription.setTitle(description.getName());
        }
        tragetDescription.setMetaDescription(description.getMetatagDescription());
        tragetDescription.setDescription(description.getDescription());
        tragetDescription.setHighlights(description.getProductHighlight());
        tragetDescription.setLanguage(description.getLanguageCode());
        tragetDescription.setKeyWords(description.getMetatagKeywords());

        tragetDescription.setLanguage(description.getLanguageCode());
        return tragetDescription;
    }

}
