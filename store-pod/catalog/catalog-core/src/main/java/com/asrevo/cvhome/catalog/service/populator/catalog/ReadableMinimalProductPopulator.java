package com.asrevo.cvhome.catalog.service.populator.catalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductFull;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadableMinimalProductPopulator extends AbstractDataPopulator<Product, StoreMerchantId, ReadableProduct> {

    private PricingService pricingService;

    private ImageFilePath imageUtils;

    @Override
    public ReadableProduct populate(Product source, ReadableProduct target, StoreMerchantId store,
                                    LanguageCode language) throws ProductNotConvertibleException {
        try {

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

            populateImages(source, target, store);

            ProductAvailability availability = populateAvailability(source, target);

            target.setSku(source.getSku());

            populatePrice(source, target, store, availability);

            if (target instanceof ReadableProductFull it) {
                it.setDescriptions(fulldescriptions);
            }

            return target;

        } catch (Exception e) {
            throw ProductNotConvertibleException.of(e);
        }
    }

    @Override
    protected ReadableProduct createTarget() {

        return null;
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

    private void populatePrice(Product source, ReadableProduct target, StoreMerchantId store, ProductAvailability availability)
            throws NoApplicableInventoryException, ProductPriceNotConvertibleException {
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
        appendProductPrice(target, availability);
    }

    private void appendProductPrice(ReadableProduct target, ProductAvailability availability) {
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

        pr.ifPresent(productPrice -> readableProductPrice.setId(productPrice.getId()));
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
