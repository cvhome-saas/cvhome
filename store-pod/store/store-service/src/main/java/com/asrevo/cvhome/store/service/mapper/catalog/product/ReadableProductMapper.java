package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.*;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.*;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductSpecification;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.core.model.references.WeightUnitOfMeasure;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final ReadableManufacturerMapper readableManufacturerMapper;

    private final PricingService pricingService;

    public ReadableProductMapper(
            ImageFilePath imageUtils,
            ReadableCategoryMapper readableCategoryMapper,
            ReadableProductTypeMapper readableProductTypeMapper,
            ReadableProductVariantMapper readableProductVariantMapper,
            ReadableManufacturerMapper readableManufacturerMapper,
            PricingService pricingService) {
        this.imageUtils = imageUtils;
        this.readableCategoryMapper = readableCategoryMapper;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.readableManufacturerMapper = readableManufacturerMapper;
        this.pricingService = pricingService;
    }

    @Override
    public ReadableProduct convert(Product source, MerchantStore store, Language language) {
        ReadableProduct product = new ReadableProduct();
        return this.merge(source, product, store, language);
    }

    @Override
    public ReadableProduct merge(
            Product source, ReadableProduct destination, MerchantStore store, Language language) {

        Assert.notNull(source, "Product cannot be null");
        Assert.notNull(destination, "Product destination cannot be null");

        // read only product values
        // will contain options
        TreeMap<Long, ReadableProductOption> selectableOptions = new TreeMap<>();

        destination.setSku(source.getSku());
        destination.setRefSku(source.getRefSku());
        destination.setId(source.getId());
        destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));

        ProductDescription description = null;
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {
            for (ProductDescription desc : source.getDescriptions()) {
                if (language != null
                        && desc.getLanguage() != null
                        && desc.getLanguage().getId().intValue() == language.getId().intValue()) {
                    description = desc;
                    break;
                }
            }
        }
        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());

        if (source.getType() != null) {
            ReadableProductType readableType =
                    readableProductTypeMapper.convert(source.getType(), store, language);
            destination.setType(readableType);
        }

        if (source.getDateAvailable() != null) {
            destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
        }

        if (source.getAuditSection() != null) {
            destination.setCreationDate(
                    DateUtil.formatDate(source.getAuditSection().getDateCreated()));
        }

        destination.setProductVirtual(source.isProductVirtual());

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        if (source.getManufacturer() != null) {
            ReadableManufacturer manufacturer =
                    readableManufacturerMapper.convert(source.getManufacturer(), store, language);
            destination.setManufacturer(manufacturer);
        }

        // images
        Set<ProductImage> images = source.getImages();
        if (CollectionUtils.isNotEmpty(images)) {

            List<ReadableImage> imageList =
                    images.stream()
                            .map(i -> this.convertImage(source, i, store))
                            .collect(Collectors.toList());
            destination.setImages(imageList);
        }

        if (!CollectionUtils.isEmpty(source.getAttributes())) {

            Set<ProductAttribute> attributes = source.getAttributes();

            if (!CollectionUtils.isEmpty(attributes)) {

                for (ProductAttribute attribute : attributes) {
                    ReadableProductOption opt = null;
                    ReadableProductAttribute attr = null;
                    ReadableProductProperty property = null;
                    ReadableProductPropertyValue propertyValue = null;
                    ReadableProductAttributeValue attrValue = new ReadableProductAttributeValue();

                    ProductOptionValue optionValue = attribute.getProductOptionValue();

                    // we need to set readonly attributes only
                    if (attribute.isAttributeDisplayOnly()) { // read only attribute = property

                        property = createProperty(attribute, language);

                        ReadableProductOption readableOption =
                                new ReadableProductOption(); // that is the property
                        ReadableProductPropertyValue readableOptionValue =
                                new ReadableProductPropertyValue();

                        readableOption.setCode(attribute.getProductOption().getCode());
                        readableOption.setId(attribute.getProductOption().getId());

                        Set<ProductOptionDescription> podescriptions =
                                attribute.getProductOption().getDescriptions();
                        if (podescriptions != null && !podescriptions.isEmpty()) {
                            for (ProductOptionDescription optionDescription : podescriptions) {
                                if (optionDescription
                                        .getLanguage()
                                        .getCode()
                                        .equals(language.getCode())) {
                                    readableOption.setName(optionDescription.getName());
                                }
                            }
                        }

                        property.setProperty(readableOption);

                        Set<ProductOptionValueDescription> povdescriptions =
                                attribute.getProductOptionValue().getDescriptions();
                        readableOptionValue.setId(attribute.getProductOptionValue().getId());
                        readableOptionValue.setCode(optionValue.getCode());
                        if (povdescriptions != null && !povdescriptions.isEmpty()) {
                            for (ProductOptionValueDescription optionValueDescription :
                                    povdescriptions) {
                                if (optionValueDescription
                                        .getLanguage()
                                        .getCode()
                                        .equals(language.getCode())) {
                                    readableOptionValue.setName(optionValueDescription.getName());
                                }
                            }
                        }

                        property.setPropertyValue(readableOptionValue);
                        destination.getProperties().add(property);

                    } else { // selectable option

                        if (selectableOptions == null) {
                            selectableOptions = new TreeMap<>();
                        }
                        opt = selectableOptions.get(attribute.getProductOption().getId());
                        if (opt == null) {
                            opt = createOption(attribute.getProductOption(), language);
                        }
                        if (opt != null) {
                            selectableOptions.put(attribute.getProductOption().getId(), opt);
                        }

                        ReadableProductOptionValue optValue = new ReadableProductOptionValue();

                        optValue.setDefaultValue(attribute.isAttributeDefault());
                        // optValue.setId(attribute.getProductOptionValue().getId());
                        optValue.setId(attribute.getId());
                        optValue.setCode(attribute.getProductOptionValue().getCode());

                        com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                        .ProductOptionValueDescription
                                valueDescription =
                                        new com.asrevo.cvhome.store.core.model.catalog.product
                                                .attribute.ProductOptionValueDescription();
                        valueDescription.setLanguage(language.getCode());
                        // optValue.setLang(language.getCode());
                        if (attribute.getProductAttributePrice() != null
                                && attribute.getProductAttributePrice().doubleValue() > 0) {
                            String formatedPrice = null;
                            try {
                                formatedPrice =
                                        pricingService.getDisplayAmount(
                                                attribute.getProductAttributePrice(), store);
                                optValue.setPrice(formatedPrice);
                            } catch (ServiceException e) {
                                throw new ConversionRuntimeException(
                                        "Error converting product option, an exception occured with"
                                                + " pricingService",
                                        e);
                            }
                        }

                        if (!StringUtils.isBlank(
                                attribute.getProductOptionValue().getProductOptionValueImage())) {
                            optValue.setImage(
                                    imageUtils.buildProductPropertyImageUtils(
                                            store,
                                            attribute
                                                    .getProductOptionValue()
                                                    .getProductOptionValueImage()));
                        }
                        optValue.setSortOrder(0);
                        if (attribute.getProductOptionSortOrder() != null) {
                            optValue.setSortOrder(attribute.getProductOptionSortOrder());
                        }

                        List<ProductOptionValueDescription> podescriptions =
                                optionValue.getDescriptionsSettoList();
                        ProductOptionValueDescription podescription = null;
                        if (podescriptions != null && !podescriptions.isEmpty()) {
                            podescription = podescriptions.getFirst();
                            if (podescriptions.size() > 1) {
                                for (ProductOptionValueDescription optionValueDescription :
                                        podescriptions) {
                                    if (optionValueDescription.getLanguage().getId().intValue()
                                            == language.getId().intValue()) {
                                        podescription = optionValueDescription;
                                        break;
                                    }
                                }
                            }
                        }
                        valueDescription.setName(podescription.getName());
                        valueDescription.setDescription(podescription.getDescription());
                        optValue.setDescription(valueDescription);

                        if (opt != null) {
                            opt.getOptionValues().add(optValue);
                        }
                    }
                }
            }
        }

        ReadableProductVariant defaultInstance = null;

        // variants
        if (!CollectionUtils.isEmpty(source.getVariants())) {
            List<ReadableProductVariant> instances =
                    source.getVariants().stream()
                            .map(i -> readableProductVariantMapper.convert(i, store, language))
                            .collect(Collectors.toList());
            destination.setVariants(instances);

            // get default instance
            defaultInstance =
                    instances.stream()
                            .filter(
                                    com.asrevo.cvhome.store.core.model.catalog.product.product
                                                    .variant.ProductVariant
                                            ::isDefaultSelection)
                            .findAny()
                            .orElse(null);

            for (ProductVariant instance : source.getVariants()) {
                instanceToOption(selectableOptions, instance, store, language);
            }
        }

        if (selectableOptions != null) {
            List<ReadableProductOption> options = new ArrayList<>(selectableOptions.values());
            destination.setOptions(options);
        }

        // availability
        ProductAvailability availability = null;
        for (ProductAvailability a : source.getAvailabilities()) {
            // TODO validate region
            // if(availability.getRegion().equals(Constants.ALL_REGIONS)) {//TODO REL 3.X
            // accept a region

            availability = a;
            destination.setQuantity(
                    availability.getProductQuantity() == null
                            ? 1
                            : availability.getProductQuantity());
            destination.setQuantityOrderMaximum(
                    availability.getProductQuantityOrderMax() == null
                            ? 1
                            : availability.getProductQuantityOrderMax());
            destination.setQuantityOrderMinimum(
                    availability.getProductQuantityOrderMin() == null
                            ? 1
                            : availability.getProductQuantityOrderMin());
            if (availability.getProductQuantity() > 0 && destination.isAvailable()) {
                destination.setCanBePurchased(true);
            }

            if (a.getProductVariant() == null && StringUtils.isEmpty(a.getRegionVariant())) {
                break;
            }
        }

        // if default instance

        destination.setSku(source.getSku());

        try {
            FinalPrice price = pricingService.calculateProductPrice(source);
            if (price != null) {

                destination.setFinalPrice(
                        pricingService.getDisplayAmount(price.getFinalPrice(), store));
                destination.setPrice(price.getFinalPrice());
                destination.setOriginalPrice(
                        pricingService.getDisplayAmount(price.getOriginalPrice(), store));

                if (price.isDiscounted()) {
                    destination.setDiscounted(true);
                }

                // price appender
                if (availability != null) {
                    Set<ProductPrice> prices = availability.getPrices();
                    if (!CollectionUtils.isEmpty(prices)) {
                        ReadableProductPrice readableProductPrice = new ReadableProductPrice();
                        readableProductPrice.setDiscounted(destination.isDiscounted());
                        readableProductPrice.setFinalPrice(destination.getFinalPrice());
                        readableProductPrice.setOriginalPrice(destination.getOriginalPrice());

                        Optional<ProductPrice> pr =
                                prices.stream()
                                        .filter(
                                                p ->
                                                        p.getCode()
                                                                .equals(
                                                                        ProductPrice
                                                                                .DEFAULT_PRICE_CODE))
                                        .findFirst();

                        destination.setProductPrice(readableProductPrice);

                        if (pr.isPresent() && language != null) {
                            readableProductPrice.setId(pr.get().getId());
                            Optional<ProductPriceDescription> d =
                                    pr.get().getDescriptions().stream()
                                            .filter(
                                                    desc ->
                                                            desc.getLanguage()
                                                                    .getCode()
                                                                    .equals(language.getCode()))
                                            .findFirst();
                            if (d.isPresent()) {
                                com.asrevo.cvhome.store.core.model.catalog.product
                                                .ProductPriceDescription
                                        priceDescription =
                                                new com.asrevo.cvhome.store.core.model.catalog
                                                        .product.ProductPriceDescription();
                                priceDescription.setLanguage(language.getCode());
                                priceDescription.setId(d.get().getId());
                                priceDescription.setPriceAppender(d.get().getPriceAppender());
                                readableProductPrice.setDescription(priceDescription);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new ConversionRuntimeException("An error while converting product price", e);
        }

        if (source.getProductReviewAvg() != null) {
            double avg = source.getProductReviewAvg().doubleValue();
            double rating = Math.round(avg * 2) / 2.0f;
            destination.setRating(rating);
        }

        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        if (description != null) {
            com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription
                    tragetDescription = populateDescription(description);
            destination.setDescription(tragetDescription);
        }

        if (!CollectionUtils.isEmpty(source.getCategories())) {
            List<ReadableCategory> categoryList = new ArrayList<>();
            for (Category category : source.getCategories()) {
                ReadableCategory readableCategory =
                        readableCategoryMapper.convert(category, store, language);
                categoryList.add(readableCategory);
            }
            destination.setCategories(categoryList);
        }

        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        if (!StringUtils.isBlank(store.getSeizeunitcode())) {
            specifications.setDimensionUnitOfMeasure(
                    DimensionUnitOfMeasure.valueOf(store.getSeizeunitcode().toLowerCase()));
        }
        if (!StringUtils.isBlank(store.getWeightunitcode())) {
            specifications.setWeightUnitOfMeasure(
                    WeightUnitOfMeasure.valueOf(store.getWeightunitcode().toLowerCase()));
        }
        destination.setProductSpecifications(specifications);

        destination.setSortOrder(source.getSortOrder());

        return destination;
    }

    private ReadableImage convertImage(Product product, ProductImage image, MerchantStore store) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(image.getProductImage());
        prdImage.setDefaultImage(image.isDefaultImage());

        // TODO product variant image
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

    private com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription
            populateDescription(ProductDescription description) {
        if (description == null) {
            return null;
        }

        com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription tragetDescription =
                new com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription();
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
        tragetDescription.setLanguage(description.getLanguage().getCode());
        tragetDescription.setKeyWords(description.getMetatagKeywords());

        if (description.getLanguage() != null) {
            tragetDescription.setLanguage(description.getLanguage().getCode());
        }
        return tragetDescription;
    }

    private ReadableProductProperty createProperty(
            ProductAttribute productAttribute, Language language) {

        ReadableProductProperty attr = new ReadableProductProperty();
        attr.setId(productAttribute.getProductOption().getId()); // attribute of the option
        attr.setType(productAttribute.getProductOption().getProductOptionType());

        List<ProductOptionDescription> descriptions =
                productAttribute.getProductOption().getDescriptionsSettoList();

        ReadableProductPropertyValue propertyValue = new ReadableProductPropertyValue();

        if (descriptions != null && !descriptions.isEmpty()) {
            for (ProductOptionDescription optionDescription : descriptions) {
                com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                .ProductOptionValueDescription
                        productOptionValueDescription =
                                new com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                        .ProductOptionValueDescription();
                productOptionValueDescription.setId(optionDescription.getId());
                productOptionValueDescription.setLanguage(
                        optionDescription.getLanguage().getCode());
                productOptionValueDescription.setName(optionDescription.getName());
                propertyValue.getValues().add(productOptionValueDescription);
            }
        }

        attr.setCode(productAttribute.getProductOption().getCode());
        return attr;
    }

    private Optional<ReadableProductOptionValue> optionValue(
            ProductOptionValue optionValue, MerchantStore store, Language language) {

        if (optionValue == null) {
            return Optional.empty();
        }

        ReadableProductOptionValue optValue = new ReadableProductOptionValue();

        com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription
                valueDescription =
                        new com.asrevo.cvhome.store.core.model.catalog.product.attribute
                                .ProductOptionValueDescription();
        valueDescription.setLanguage(language.getCode());

        if (!StringUtils.isBlank(optionValue.getProductOptionValueImage())) {
            optValue.setImage(
                    imageUtils.buildProductPropertyImageUtils(
                            store, optionValue.getProductOptionValueImage()));
        }
        optValue.setSortOrder(0);

        if (optionValue.getProductOptionValueSortOrder() != null) {
            optValue.setSortOrder(optionValue.getProductOptionValueSortOrder());
        }

        optValue.setCode(optionValue.getCode());

        List<ProductOptionValueDescription> podescriptions = optionValue.getDescriptionsSettoList();
        ProductOptionValueDescription podescription = null;
        if (podescriptions != null && !podescriptions.isEmpty()) {
            podescription = podescriptions.getFirst();
            if (podescriptions.size() > 1) {
                for (ProductOptionValueDescription optionValueDescription : podescriptions) {
                    if (optionValueDescription.getLanguage().getId().intValue()
                            == language.getId().intValue()) {
                        podescription = optionValueDescription;
                        break;
                    }
                }
            }
        }
        valueDescription.setName(podescription.getName());
        valueDescription.setDescription(podescription.getDescription());
        optValue.setDescription(valueDescription);

        return Optional.of(optValue);
    }

    private void instanceToOption(
            TreeMap<Long, ReadableProductOption> selectableOptions,
            ProductVariant instance,
            MerchantStore store,
            Language language) {

        ReadableProductOption option =
                this.option(
                        selectableOptions, instance.getVariation().getProductOption(), language);
        option.setVariant(true);

        // take care of option value
        Optional<ReadableProductOptionValue> optionOptionValue =
                this.optionValue(instance.getVariation().getProductOptionValue(), store, language);

        if (optionOptionValue.isPresent()) {
            optionOptionValue.get().setId(instance.getId());
            if (instance.isDefaultSelection()) {
                optionOptionValue.get().setDefaultValue(true);
            }
            addOptionValue(option, optionOptionValue.get());
        }

        if (instance.getVariationValue() != null) {
            ReadableProductOption optionValue =
                    this.option(
                            selectableOptions,
                            instance.getVariationValue().getProductOption(),
                            language);

            // take care of option value
            Optional<ReadableProductOptionValue> optionValueOptionValue =
                    this.optionValue(
                            instance.getVariationValue().getProductOptionValue(), store, language);

            if (optionValueOptionValue.isPresent()) {
                optionValueOptionValue.get().setId(instance.getId());
                if (instance.isDefaultSelection()) {
                    optionValueOptionValue.get().setDefaultValue(true);
                }
                addOptionValue(optionValue, optionValueOptionValue.get());
            }
        }
    }

    private void addOptionValue(
            ReadableProductOption option, ReadableProductOptionValue optionValue) {

        ReadableProductOptionValue find =
                option.getOptionValues().stream()
                        .filter(optValue -> optValue.getCode().equals(optionValue.getCode()))
                        .findAny()
                        .orElse(null);

        if (find == null) {
            option.getOptionValues().add(optionValue);
        }
    }

    private ReadableProductOption option(
            TreeMap<Long, ReadableProductOption> selectableOptions,
            ProductOption option,
            Language language) {
        if (selectableOptions.containsKey(option.getId())) {
            return selectableOptions.get(option.getId());
        }

        ReadableProductOption readable = this.createOption(option, language);
        selectableOptions.put(readable.getId(), readable);
        return readable;
    }

    private ReadableProductOption createOption(ProductOption opt, Language language) {

        ReadableProductOption option = new ReadableProductOption();
        option.setId(opt.getId()); // attribute of the option
        option.setType(opt.getProductOptionType());
        option.setCode(opt.getCode());
        List<ProductOptionDescription> descriptions = opt.getDescriptionsSettoList();
        ProductOptionDescription description = null;
        if (descriptions != null && !descriptions.isEmpty()) {
            description = descriptions.getFirst();
            if (descriptions.size() > 1) {
                for (ProductOptionDescription optionDescription : descriptions) {
                    if (optionDescription.getLanguage().getCode().equals(language.getCode())) {
                        description = optionDescription;
                        break;
                    }
                }
            }
        }

        if (description == null) {
            return null;
        }

        option.setLang(language.getCode());
        option.setName(description.getName());
        option.setCode(opt.getCode());

        return option;
    }
}
