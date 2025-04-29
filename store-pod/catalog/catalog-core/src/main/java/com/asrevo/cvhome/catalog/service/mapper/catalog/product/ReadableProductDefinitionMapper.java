package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

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
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinitionFull;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.catalog.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.model.references.WeightUnitOfMeasure;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ReadableProductDefinitionMapper implements Mapper<Product, ReadableProductDefinition> {

    private final ReadableCategoryMapper readableCategoryMapper;

    private final ReadableProductTypeMapper readableProductTypeMapper;

    private final ReadableManufacturerMapper readableManufacturerMapper;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final ImageFilePath imageUtils;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableProductDefinitionMapper(
            ReadableCategoryMapper readableCategoryMapper,
            ReadableProductTypeMapper readableProductTypeMapper,
            ReadableManufacturerMapper readableManufacturerMapper,
            ReadableInventoryMapper readableInventoryMapper,
            ImageFilePath imageUtils,
            ExternalMerchantStoreService externalMerchantStoreService) {
        this.readableCategoryMapper = readableCategoryMapper;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.readableManufacturerMapper = readableManufacturerMapper;
        this.readableInventoryMapper = readableInventoryMapper;
        this.imageUtils = imageUtils;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableProductDefinition convert(
            Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProductDefinition target = new ReadableProductDefinition();
        return this.merge(source, target, store, language);
    }

    @Override
    public ReadableProductDefinition merge(
            Product source,
            ReadableProductDefinition destination,
            StoreMerchantId store,
            LanguageCode language) {
        Assert.notNull(source, "Product cannot be null");
        Assert.notNull(destination, "Product destination cannot be null");

        ReadableProductDefinition returnDestination = destination;
        if (language == null) {
            returnDestination = new ReadableProductDefinitionFull();
        }

        List<com.asrevo.cvhome.catalog.model.product.ProductDescription> fulldescriptions =
                new ArrayList<>();

        returnDestination.setIdentifier(source.getSku());
        returnDestination.setId(source.getId());
        returnDestination.setVisible(source.isAvailable());
        returnDestination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
        returnDestination.setSku(source.getSku());
        ProductDescription description = null;
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {
            for (ProductDescription desc : source.getDescriptions()) {
                if (Objects.equals(desc.getLanguageCode(), language)) {
                    description = desc;
                    break;
                } else {
                    fulldescriptions.add(populateDescription(desc));
                }
            }
        }

        /*		if (source.getProductReviewAvg() != null) {
        	double avg = source.getProductReviewAvg().doubleValue();
        	double rating = Math.round(avg * 2) / 2.0f;
        	returnDestination.setRating(rating);
        }

        if (source.getProductReviewCount() != null) {
        	returnDestination.setRatingCount(source.getProductReviewCount().intValue());
        }*/

        if (description != null) {
            com.asrevo.cvhome.catalog.model.product.ProductDescription tragetDescription =
                    populateDescription(description);
            returnDestination.setDescription(tragetDescription);
        }

        if (source.getManufacturer() != null) {
            ReadableManufacturer manufacturer =
                    readableManufacturerMapper.convert(source.getManufacturer(), store, language);
            returnDestination.setManufacturer(manufacturer);
        }

        if (!CollectionUtils.isEmpty(source.getCategories())) {
            List<ReadableCategory> categoryList = new ArrayList<>();
            for (Category category : source.getCategories()) {
                ReadableCategory readableCategory =
                        readableCategoryMapper.convert(category, store, language);
                categoryList.add(readableCategory);
            }
            returnDestination.setCategories(categoryList);
        }

        ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);
        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        specifications.setDimensionUnitOfMeasure(
                DimensionUnitOfMeasure.valueOf(baseStore.getDimension().name().toLowerCase()));
        specifications.setWeightUnitOfMeasure(
                WeightUnitOfMeasure.valueOf(baseStore.getWeight().name().toLowerCase()));

        returnDestination.setProductSpecifications(specifications);

        if (source.getType() != null) {
            ReadableProductType readableType =
                    readableProductTypeMapper.convert(source.getType(), store, language);
            returnDestination.setType(readableType);
        }

        returnDestination.setSortOrder(source.getSortOrder());

        // images
        Set<ProductImage> images = source.getImages();
        if (CollectionUtils.isNotEmpty(images)) {

            List<ReadableImage> imageList =
                    images.stream()
                            .map(i -> this.convertImage(source, i, store))
                            .collect(Collectors.toList());
            returnDestination.setImages(imageList);
        }

        // quantity
        ProductAvailability availability = null;
        for (ProductAvailability a : source.getAvailabilities()) {
            availability = a;
        }

        if (availability != null) {
            returnDestination.setCanBePurchased(availability.isProductStatus());
            ReadableInventory inventory =
                    readableInventoryMapper.convert(availability, store, language);
            returnDestination.setInventory(inventory);
        }

        if (returnDestination instanceof ReadableProductDefinitionFull) {
            ((ReadableProductDefinitionFull) returnDestination).setDescriptions(fulldescriptions);
        }

        return returnDestination;
    }

    private ReadableImage convertImage(Product product, ProductImage image, StoreMerchantId store) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(image.getProductImage());
        prdImage.setDefaultImage(image.isDefaultImage());

        StringBuilder imgPath = new StringBuilder();
        imgPath.append(imageUtils.getContextPath())
                .append(
                        imageUtils.buildProductImageUtils(
                                store, product.getSku(), image.getProductImage()));

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
