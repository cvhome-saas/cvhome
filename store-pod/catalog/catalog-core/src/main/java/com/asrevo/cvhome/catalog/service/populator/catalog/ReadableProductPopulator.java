package com.asrevo.cvhome.catalog.service.populator.catalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductFull;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductProperty;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductPropertyValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.model.product.type.ProductTypeDescription;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadableProductPopulator extends AbstractDataPopulator<Product, StoreMerchantId, ReadableProduct> {

    private PricingService pricingService;

    private ImageFilePath imageUtils;

    private ExternalMerchantStoreService externalMerchantStoreService;

    @Override
    public ReadableProduct populate(Product source, ReadableProduct target, StoreMerchantId store,
                                    LanguageCode language) throws ConversionException {
        try {
            ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);
            List<com.asrevo.cvhome.catalog.model.product.ProductDescription> fulldescriptions = new ArrayList<>();
            if (language == null) {
                target = new ReadableProductFull();
            }

            if (target == null) {
                target = new ReadableProduct();
            }

            ProductDescription description = resolveDescription(source, language, fulldescriptions);

            if (target instanceof ReadableProductFull it) {
                it.setDescriptions(fulldescriptions);
            }

            if (language == null) {
                language = baseStore.getDefaultLanguage();
            }

            final LanguageCode lang = language;

            target.setId(source.getId());
            target.setAvailable(source.isAvailable());
            target.setProductShipeable(source.isProductShipeable());

            ProductSpecification specifications = new ProductSpecification();
            specifications.setHeight(source.getProductHeight());
            specifications.setLength(source.getProductLength());
            specifications.setWeight(source.getProductWeight());
            specifications.setWidth(source.getProductWidth());
            target.setProductSpecifications(specifications);

            target.setPreOrder(source.isPreOrder());
            target.setRefSku(source.getRefSku());
            target.setSortOrder(source.getSortOrder());

            if (source.getType() != null) {
                target.setType(this.type(source.getType(), language));
            }

            if (source.getDateAvailable() != null) {
                target.setDateAvailable(source.getDateAvailable());
            }

            if (source.getAuditSection() != null) {
                target.setCreationDate(source.getAuditSection().getDateCreated());
            }


            target.setProductVirtual(source.isProductVirtual());

            if (description != null) {
                com.asrevo.cvhome.catalog.model.product.ProductDescription tragetDescription = populateDescription(
                        description);
                target.setDescription(tragetDescription);
            }

            populateManufacturer(source, target);
            populateImages(source, target, store);
            populateCategories(source, target, store, language);
            populateAttributes(source, target, store, language);

            ProductAvailability availability = populateAvailability(source, target);

            target.setSku(source.getSku());

            populatePrice(source, target, store, availability, lang);

            if (target instanceof ReadableProductFull it) {
                it.setDescriptions(fulldescriptions);
            }

            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    private ProductDescription resolveDescription(Product source, LanguageCode language,
                                                  List<com.asrevo.cvhome.catalog.model.product.ProductDescription> fulldescriptions) {
        ProductDescription description = source.getProductDescription();
        if (source.getDescriptions() == null || source.getDescriptions().isEmpty()) {
            return description;
        }
        for (ProductDescription desc : source.getDescriptions()) {
            if (language != null && Objects.equals(desc.getLanguageCode(), language)) {
                description = desc;
                break;
            } else {
                fulldescriptions.add(populateDescription(desc));
            }
        }
        return description;
    }

    private void populateManufacturer(Product source, ReadableProduct target) {
        if (source.getManufacturer() == null) {
            return;
        }
        ManufacturerDescription manufacturer = source.getManufacturer().getDescriptions().iterator().next();
        ReadableManufacturer manufacturerEntity = new ReadableManufacturer();
        com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription d =
                new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();
        d.setName(manufacturer.getName());
        manufacturerEntity.setDescription(d);
        manufacturerEntity.setId(source.getManufacturer().getId());
        manufacturerEntity.setOrder(source.getManufacturer().getOrder());
        manufacturerEntity.setCode(source.getManufacturer().getCode());
        target.setManufacturer(manufacturerEntity);
    }

    private void populateImages(Product source, ReadableProduct target, StoreMerchantId store) {
        Set<ProductImage> images = source.getImages();
        if (images == null || images.isEmpty()) {
            return;
        }

        String contextPath = imageUtils.getContextPath();
        List<ReadableImage> imageList = new ArrayList<>();
        for (ProductImage img : images) {
            imageList.add(buildReadableImage(img, source, store, contextPath, target));
        }
        imageList = imageList.stream()
                .sorted(Comparator.comparingInt(ReadableImage::getOrder))
                .toList();

        target.setImages(imageList);
    }

    private ReadableImage buildReadableImage(ProductImage img, Product source, StoreMerchantId store, String contextPath,
                                             ReadableProduct target) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(img.getProductImage());
        prdImage.setDefaultImage(img.isDefaultImage());
        prdImage.setOrder(img.getSortOrder() != null ? img.getSortOrder() : 0);

        if (img.getImageType() == 1 && img.getProductImageUrl() != null) {
            prdImage.setImageUrl(img.getProductImageUrl());
        } else {
            StringBuilder imgPath = new StringBuilder();
            imgPath.append(contextPath)
                    .append(imageUtils.buildProductImageUtils(store, source.getSku(), img.getProductImage()));

            prdImage.setImageUrl(imgPath.toString());
        }
        prdImage.setId(img.getId());
        prdImage.setImageType(img.getImageType());
        if (img.getProductImageUrl() != null) {
            prdImage.setExternalUrl(img.getProductImageUrl());
        }
        if (img.getImageType() == 1 && img.getProductImageUrl() != null) { // video
            prdImage.setVideoUrl(img.getProductImageUrl());
        }

        if (prdImage.isDefaultImage()) {
            target.setImage(prdImage);
        }

        return prdImage;
    }

    private void populateCategories(Product source, ReadableProduct target, StoreMerchantId store, LanguageCode language) {
        if (CollectionUtils.isEmpty(source.getCategories())) {
            return;
        }

        ReadableCategoryPopulator categoryPopulator = new ReadableCategoryPopulator();
        List<ReadableCategory> categoryList = new ArrayList<>();

        for (Category category : source.getCategories()) {
            ReadableCategory readableCategory = new ReadableCategory();
            categoryPopulator.populate(category, readableCategory, store, language);
            categoryList.add(readableCategory);
        }

        target.setCategories(categoryList);
    }

    private void populateAttributes(Product source, ReadableProduct target, StoreMerchantId store, LanguageCode language)
            throws ServiceException {
        if (CollectionUtils.isEmpty(source.getAttributes())) {
            return;
        }

        Map<Long, ReadableProductOption> selectableOptions = null;

        for (ProductAttribute attribute : source.getAttributes()) {
            if (attribute.isAttributeDisplayOnly()) {
                target.getProperties().add(buildDisplayOnlyProperty(attribute, language));
                continue;
            }
            selectableOptions = applySelectableAttribute(attribute, selectableOptions, store, language);
        }

        if (selectableOptions != null) {
            target.setOptions(new ArrayList<>(selectableOptions.values()));
        }
    }

    private ReadableProductProperty buildDisplayOnlyProperty(ProductAttribute attribute, LanguageCode language) {
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

    private Map<Long, ReadableProductOption> applySelectableAttribute(ProductAttribute attribute,
                                                                      Map<Long, ReadableProductOption> selectableOptions,
                                                                      StoreMerchantId store, LanguageCode language)
            throws ServiceException {
        Map<Long, ReadableProductOption> options = selectableOptions;
        if (options == null) {
            options = new TreeMap<>();
        }
        ReadableProductOption opt = options.get(attribute.getProductOption().getId());
        if (opt == null) {
            opt = createOption(attribute, language);
        }
        if (opt != null) {
            options.put(attribute.getProductOption().getId(), opt);
        }

        ReadableProductOptionValue optValue = buildOptionValue(attribute, store, language);

        if (opt != null) {
            opt.getOptionValues().add(optValue);
        }
        return options;
    }

    private ReadableProductOptionValue buildOptionValue(ProductAttribute attribute, StoreMerchantId store, LanguageCode language)
            throws ServiceException {
        ReadableProductOptionValue optValue = new ReadableProductOptionValue();
        ProductOptionValue optionValue = attribute.getProductOptionValue();

        optValue.setDefaultValue(attribute.isAttributeDefault());
        optValue.setId(attribute.getId());
        optValue.setCode(attribute.getProductOptionValue().getCode());

        com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription valueDescription =
                new com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription();
        valueDescription.setLanguage(language);

        applyAttributePrice(attribute, store, optValue);
        applyAttributeImage(attribute, store, optValue);

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

    private void applyAttributePrice(ProductAttribute attribute, StoreMerchantId store, ReadableProductOptionValue optValue)
            throws ServiceException {
        if (attribute.getProductAttributePrice() == null || attribute.getProductAttributePrice().doubleValue() <= 0) {
            return;
        }
        String formatedPrice = pricingService.getDisplayAmount(attribute.getProductAttributePrice(), store);
        optValue.setPrice(formatedPrice);
    }

    private void applyAttributeImage(ProductAttribute attribute, StoreMerchantId store, ReadableProductOptionValue optValue) {
        if (StringUtils.isBlank(attribute.getProductOptionValue().getProductOptionValueImage())) {
            return;
        }
        optValue.setImage(imageUtils.buildProductPropertyImageUtils(store,
                attribute.getProductOptionValue().getProductOptionValueImage()));
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

    private ProductAvailability populateAvailability(Product source, ReadableProduct target) {
        ProductAvailability availability = null;
        for (ProductAvailability a : source.getAvailabilities()) {
            availability = a;
            target.setQuantity(availability.getProductQuantity() == null ? 1 : availability.getProductQuantity());
            target.setQuantityOrderMaximum(availability.getProductQuantityOrderMax() == null ? 1
                    : availability.getProductQuantityOrderMax());
            target.setQuantityOrderMinimum(availability.getProductQuantityOrderMin() == null ? 1
                    : availability.getProductQuantityOrderMin());
            if (availability.getProductQuantity() > 0 && target.isAvailable()) {
                target.setCanBePurchased(true);
            }
        }
        return availability;
    }

    private void populatePrice(Product source, ReadableProduct target, StoreMerchantId store, ProductAvailability availability,
                               LanguageCode lang) throws ServiceException {
        FinalPriceCalc price = pricingService.calculateProductPrice(source);
        if (price == null) {
            return;
        }

        target.setFinalPrice(pricingService.getDisplayAmount(price.getFinalPrice(), store));
        target.setPrice(price.getFinalPrice());
        target.setOriginalPrice(pricingService.getDisplayAmount(price.getOriginalPrice(), store));

        if (price.isDiscounted()) {
            target.setDiscounted(true);
        }

        // price appender
        appendProductPrice(target, availability, lang);
    }

    private void appendProductPrice(ReadableProduct target, ProductAvailability availability, LanguageCode lang) {
        if (availability == null) {
            return;
        }
        Set<ProductPrice> prices = availability.getPrices();
        if (CollectionUtils.isEmpty(prices)) {
            return;
        }
        ReadableProductPrice readableProductPrice = new ReadableProductPrice();
        readableProductPrice.setDiscounted(target.isDiscounted());
        readableProductPrice.setFinalPrice(target.getFinalPrice());
        readableProductPrice.setOriginalPrice(target.getOriginalPrice());

        Optional<ProductPrice> pr = prices.stream()
                .filter(p -> p.getCode().equals(ProductPrice.DEFAULT_PRICE_CODE))
                .findFirst();

        target.setProductPrice(readableProductPrice);

        pr.ifPresent(productPrice -> applyPriceDescription(productPrice, readableProductPrice, lang));
    }

    private void applyPriceDescription(ProductPrice productPrice, ReadableProductPrice readableProductPrice, LanguageCode lang) {
        readableProductPrice.setId(productPrice.getId());
        Optional<ProductPriceDescription> d = productPrice.getDescriptions()
                .stream()
                .filter(desc -> desc.getLanguageCode().equals(lang))
                .findFirst();
        d.ifPresent(desc -> {
            com.asrevo.cvhome.catalog.model.product.ProductPriceDescription priceDescription =
                    new com.asrevo.cvhome.catalog.model.product.ProductPriceDescription();
            priceDescription.setLanguage(lang);
            priceDescription.setId(desc.getId());
            priceDescription.setPriceAppender(desc.getPriceAppender());
            readableProductPrice.setDescription(priceDescription);
        });
    }

    private ReadableProductOption createOption(ProductAttribute productAttribute, LanguageCode language) {

        ReadableProductOption option = new ReadableProductOption();
        option.setId(productAttribute.getProductOption().getId()); // attribute of the
        // option
        option.setType(productAttribute.getProductOption().getProductOptionType());
        option.setCode(productAttribute.getProductOption().getCode());
        ProductOptionDescription description = resolveOptionDescription(productAttribute, language);

        if (description == null) {
            return null;
        }

        option.setLang(language);
        option.setName(description.getName());
        option.setCode(productAttribute.getProductOption().getCode());

        return option;
    }

    private ProductOptionDescription resolveOptionDescription(ProductAttribute productAttribute, LanguageCode language) {
        List<ProductOptionDescription> descriptions = productAttribute.getProductOption().getDescriptionsSettoList();
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

    private ReadableProductType type(ProductType type, LanguageCode language) {
        ReadableProductType readableType = new ReadableProductType();
        readableType.setCode(type.getCode());
        readableType.setId(type.getId());

        if (!CollectionUtils.isEmpty(type.getDescriptions())) {
            Optional<ProductTypeDescription> desc = type.getDescriptions()
                    .stream()
                    .filter(t -> t.getLanguageCode().equals(language))
                    .map(this::typeDescription)
                    .findFirst();
            desc.ifPresent(readableType::setDescription);
        }

        return readableType;
    }

    private ProductTypeDescription typeDescription(
            com.asrevo.cvhome.catalog.entity.product.type.ProductTypeDescription description) {
        ProductTypeDescription desc = new ProductTypeDescription();
        desc.setId(description.getId());
        desc.setName(description.getName());
        desc.setDescription(description.getDescription());
        desc.setLanguage(description.getLanguageCode());
        return desc;
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

    @Override
    protected ReadableProduct createTarget() {

        return null;
    }

    com.asrevo.cvhome.catalog.model.product.ProductDescription populateDescription(ProductDescription description) {
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
