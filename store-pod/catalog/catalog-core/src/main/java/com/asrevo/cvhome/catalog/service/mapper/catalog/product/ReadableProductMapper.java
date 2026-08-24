package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductProperty;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductPropertyValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.model.references.WeightUnitOfMeasure;
import com.asrevo.cvhome.store.utils.ImageFilePath;

/**
 * Works for product v2 model
 *
 * @author carlsamson
 */
@Component
public class ReadableProductMapper implements Mapper<Product, ReadableProduct> {

    // uses code that is similar to ProductDefinition
    private final ImageFilePath imageUtils;

    private final ReadableCategoryMapper readableCategoryMapper;

    private final ReadableProductTypeMapper readableProductTypeMapper;

    private final ReadableManufacturerMapper readableManufacturerMapper;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ReadableProductMapper(ImageFilePath imageUtils, ReadableCategoryMapper readableCategoryMapper,
                                 ReadableProductTypeMapper readableProductTypeMapper,
                                 ReadableManufacturerMapper readableManufacturerMapper,
                                 ExternalMerchantStoreService externalMerchantStoreService) {
        this.imageUtils = imageUtils;
        this.readableCategoryMapper = readableCategoryMapper;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.readableManufacturerMapper = readableManufacturerMapper;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public ReadableProduct convert(Product source, StoreMerchantId store, LanguageCode language) {
        ReadableProduct product = new ReadableProduct();
        return this.merge(source, product, store, language);
    }

    @Override
    public ReadableProduct merge(Product source, ReadableProduct destination, StoreMerchantId store,
                                 LanguageCode language) {
        TreeMap<Long, ReadableProductOption> selectableOptions = new TreeMap<>();

        destination.setSku(source.getSku());
        destination.setRefSku(source.getRefSku());
        destination.setId(source.getId());
        destination.setDateAvailable(source.getDateAvailable());

        ProductDescription description = resolveDescription(source, language);
        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());

        if (source.getType() != null) {
            ReadableProductType readableType = readableProductTypeMapper.convert(source.getType(), store, language);
            destination.setType(readableType);
        }

        if (source.getDateAvailable() != null) {
            destination.setDateAvailable(source.getDateAvailable());
        }

        if (source.getAuditSection() != null) {
            destination.setCreationDate(source.getAuditSection().getDateCreated());
        }

        destination.setProductVirtual(source.isProductVirtual());

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        if (source.getManufacturer() != null) {
            ReadableManufacturer manufacturer = readableManufacturerMapper.convert(source.getManufacturer(), store,
                    language);
            destination.setManufacturer(manufacturer);
        }

        populateImages(source, destination, store);
        populateAttributes(source, destination, selectableOptions, store, language);

        List<ReadableProductOption> options = new ArrayList<>(selectableOptions.values());
        destination.setOptions(options);

        destination.setSku(source.getSku());

        if (source.getProductReviewAvg() != null) {
            double avg = source.getProductReviewAvg().doubleValue();
            double rating = Math.round(avg * 2) / 2.0f;
            destination.setRating(rating);
        }

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        if (description != null) {
            com.asrevo.cvhome.catalog.model.product.ProductDescription tragetDescription = populateDescription(
                    description);
            destination.setDescription(tragetDescription);
        }

        populateCategories(source, destination, store, language);

        ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);

        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        specifications
                .setDimensionUnitOfMeasure(DimensionUnitOfMeasure.valueOf(baseStore.getDimension().name().toLowerCase()));
        specifications.setWeightUnitOfMeasure(WeightUnitOfMeasure.valueOf(baseStore.getWeight().name().toLowerCase()));
        destination.setProductSpecifications(specifications);

        destination.setSortOrder(source.getSortOrder());

        return destination;
    }

    private ProductDescription resolveDescription(Product source, LanguageCode language) {
        if (source.getDescriptions() == null || source.getDescriptions().isEmpty()) {
            return null;
        }
        for (ProductDescription desc : source.getDescriptions()) {
            if (Objects.equals(desc.getLanguageCode(), language)) {
                return desc;
            }
        }
        return null;
    }

    private void populateImages(Product source, ReadableProduct destination, StoreMerchantId store) {
        Set<ProductImage> images = source.getImages();
        if (!CollectionUtils.isNotEmpty(images)) {
            return;
        }

        List<ReadableImage> imageList = images.stream()
                .map(i -> this.convertImage(source, i, store))
                .toList();
        destination.setImages(imageList);
        destination.setImage(imageList.getFirst());
    }

    private void populateAttributes(Product source, ReadableProduct destination,
                                    TreeMap<Long, ReadableProductOption> selectableOptions, StoreMerchantId store, LanguageCode language) {
        if (CollectionUtils.isEmpty(source.getAttributes())) {
            return;
        }

        for (ProductAttribute attribute : source.getAttributes()) {
            if (attribute.isAttributeDisplayOnly()) {
                destination.getProperties().add(buildDisplayOnlyProperty(attribute, language));
                continue;
            }
            applySelectableAttribute(attribute, selectableOptions, store, language);
        }
    }

    private ReadableProductProperty buildDisplayOnlyProperty(ProductAttribute attribute, LanguageCode language) {
        ProductOptionValue optionValue = attribute.getProductOptionValue();
        ReadableProductProperty property = createProperty(attribute);

        ReadableProductOption readableOption = new ReadableProductOption();
        ReadableProductPropertyValue readableOptionValue = new ReadableProductPropertyValue();

        readableOption.setCode(attribute.getProductOption().getCode());
        readableOption.setId(attribute.getProductOption().getId());

        Set<ProductOptionDescription> podescriptions = attribute.getProductOption().getDescriptions();
        applyMatchingOptionName(podescriptions, language, readableOption);

        property.setProperty(readableOption);

        Set<ProductOptionValueDescription> povdescriptions = attribute.getProductOptionValue().getDescriptions();
        readableOptionValue.setId(attribute.getProductOptionValue().getId());
        readableOptionValue.setCode(optionValue.getCode());
        applyMatchingOptionValueName(povdescriptions, language, readableOptionValue);

        property.setPropertyValue(readableOptionValue);
        return property;
    }

    private void applyMatchingOptionName(Set<ProductOptionDescription> podescriptions, LanguageCode language,
                                         ReadableProductOption readableOption) {
        if (podescriptions == null || podescriptions.isEmpty()) {
            return;
        }
        for (ProductOptionDescription optionDescription : podescriptions) {
            if (optionDescription.getLanguageCode().equals(language)) {
                readableOption.setName(optionDescription.getName());
            }
        }
    }

    private void applyMatchingOptionValueName(Set<ProductOptionValueDescription> povdescriptions, LanguageCode language,
                                              ReadableProductPropertyValue readableOptionValue) {
        if (povdescriptions == null || povdescriptions.isEmpty()) {
            return;
        }
        for (ProductOptionValueDescription optionValueDescription : povdescriptions) {
            if (optionValueDescription.getLanguageCode().equals(language)) {
                readableOptionValue.setName(optionValueDescription.getName());
            }
        }
    }

    private void applySelectableAttribute(ProductAttribute attribute, TreeMap<Long, ReadableProductOption> selectableOptions,
                                          StoreMerchantId store, LanguageCode language) {
        ReadableProductOption opt = selectableOptions.get(attribute.getProductOption().getId());
        if (opt == null) {
            opt = createOption(attribute.getProductOption(), language);
        }
        if (opt != null) {
            selectableOptions.put(attribute.getProductOption().getId(), opt);
        }

        ReadableProductOptionValue optValue = buildOptionValue(attribute, store, language);

        if (opt != null) {
            opt.getOptionValues().add(optValue);
        }
    }

    private ReadableProductOptionValue buildOptionValue(ProductAttribute attribute, StoreMerchantId store, LanguageCode language) {
        ProductOptionValue optionValue = attribute.getProductOptionValue();
        ReadableProductOptionValue optValue = new ReadableProductOptionValue();

        optValue.setDefaultValue(attribute.isAttributeDefault());
        optValue.setId(attribute.getId());
        optValue.setCode(attribute.getProductOptionValue().getCode());

        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription valueDescription =
                new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription();
        valueDescription.setLanguage(language);

        if (!StringUtils.isBlank(attribute.getProductOptionValue().getProductOptionValueImage())) {
            optValue.setImage(imageUtils.buildProductPropertyImageUtils(store,
                    attribute.getProductOptionValue().getProductOptionValueImage()));
        }
        optValue.setSortOrder(0);
        if (attribute.getProductOptionSortOrder() != null) {
            optValue.setSortOrder(attribute.getProductOptionSortOrder());
        }

        ProductOptionValueDescription podescription = resolveOptionValueDescription(optionValue, language);
        if (podescription != null) {
            valueDescription.setName(podescription.getName());
            valueDescription.setDescription(podescription.getDescription());
        }
        optValue.setDescription(valueDescription);
        return optValue;
    }

    private ProductOptionValueDescription resolveOptionValueDescription(ProductOptionValue optionValue, LanguageCode language) {
        List<ProductOptionValueDescription> podescriptions = optionValue.getDescriptionsSettoList();
        if (podescriptions == null || podescriptions.isEmpty()) {
            return null;
        }
        ProductOptionValueDescription podescription = podescriptions.getFirst();
        if (podescriptions.size() <= 1) {
            return podescription;
        }
        for (ProductOptionValueDescription optionValueDescription : podescriptions) {
            if (Objects.equals(optionValueDescription.getLanguageCode(), language)) {
                return optionValueDescription;
            }
        }
        return podescription;
    }

    private void populateCategories(Product source, ReadableProduct destination, StoreMerchantId store, LanguageCode language) {
        if (CollectionUtils.isEmpty(source.getCategories())) {
            return;
        }
        List<ReadableCategory> categoryList = new ArrayList<>();
        for (Category category : source.getCategories()) {
            ReadableCategory readableCategory = readableCategoryMapper.convert(category, store, language);
            categoryList.add(readableCategory);
        }
        destination.setCategories(categoryList);
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

        if (description.getLanguageCode() != null) {
            tragetDescription.setLanguage(description.getLanguageCode());
        }
        return tragetDescription;
    }

    private ReadableProductProperty createProperty(ProductAttribute productAttribute) {

        ReadableProductProperty attr = new ReadableProductProperty();
        attr.setId(productAttribute.getProductOption().getId()); // attribute of the
        // option
        attr.setType(productAttribute.getProductOption().getProductOptionType());

        List<ProductOptionDescription> descriptions = productAttribute.getProductOption().getDescriptionsSettoList();

        ReadableProductPropertyValue propertyValue = new ReadableProductPropertyValue();

        if (descriptions != null && !descriptions.isEmpty()) {
            for (ProductOptionDescription optionDescription : descriptions) {
                com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription productOptionValueDescription =
                        new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription();
                productOptionValueDescription.setId(optionDescription.getId());
                productOptionValueDescription.setLanguage(optionDescription.getLanguageCode());
                productOptionValueDescription.setName(optionDescription.getName());
                propertyValue.getValues().add(productOptionValueDescription);
            }
        }

        attr.setCode(productAttribute.getProductOption().getCode());
        return attr;
    }

    private Optional<ReadableProductOptionValue> optionValue(ProductOptionValue optionValue, StoreMerchantId store,
                                                             LanguageCode language) {

        if (optionValue == null) {
            return Optional.empty();
        }

        ReadableProductOptionValue optValue = new ReadableProductOptionValue();

        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription valueDescription =
                new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription();
        valueDescription.setLanguage(language);

        if (!StringUtils.isBlank(optionValue.getProductOptionValueImage())) {
            optValue
                    .setImage(imageUtils.buildProductPropertyImageUtils(store, optionValue.getProductOptionValueImage()));
        }
        optValue.setSortOrder(0);

        if (optionValue.getProductOptionValueSortOrder() != null) {
            optValue.setSortOrder(optionValue.getProductOptionValueSortOrder());
        }

        optValue.setCode(optionValue.getCode());

        ProductOptionValueDescription podescription = resolveOptionValueDescription(optionValue, language);
        if (podescription != null) {
            valueDescription.setName(podescription.getName());
            valueDescription.setDescription(podescription.getDescription());
        }
        optValue.setDescription(valueDescription);

        return Optional.of(optValue);
    }

    private ReadableProductOption option(TreeMap<Long, ReadableProductOption> selectableOptions, ProductOption option,
                                         LanguageCode language) {
        if (selectableOptions.containsKey(option.getId())) {
            return selectableOptions.get(option.getId());
        }

        ReadableProductOption readable = this.createOption(option, language);
        if (readable != null) {
            selectableOptions.put(readable.getId(), readable);
        }
        return readable;
    }

    private ReadableProductOption createOption(ProductOption opt, LanguageCode language) {

        ReadableProductOption option = new ReadableProductOption();
        option.setId(opt.getId()); // attribute of the option
        option.setType(opt.getProductOptionType());
        option.setCode(opt.getCode());
        ProductOptionDescription description = resolveOptionDescription(opt, language);

        if (description == null) {
            return null;
        }

        option.setLang(language);
        option.setName(description.getName());
        option.setCode(opt.getCode());

        return option;
    }

    private ProductOptionDescription resolveOptionDescription(ProductOption opt, LanguageCode language) {
        List<ProductOptionDescription> descriptions = opt.getDescriptionsSettoList();
        if (descriptions == null || descriptions.isEmpty()) {
            return null;
        }
        ProductOptionDescription description = descriptions.getFirst();
        if (descriptions.size() <= 1) {
            return description;
        }
        for (ProductOptionDescription optionDescription : descriptions) {
            if (optionDescription.getLanguageCode().equals(language)) {
                return optionDescription;
            }
        }
        return description;
    }

}
