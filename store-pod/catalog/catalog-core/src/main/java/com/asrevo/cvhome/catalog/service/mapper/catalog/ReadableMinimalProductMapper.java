package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.errors.NoApplicableInventoryException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableMinimalProductMapper implements Mapper<Product, ReadableMinimalProduct> {

    private final PricingService pricingService;

    private final ImageFilePath imageUtils;

    public ReadableMinimalProductMapper(PricingService pricingService, ImageFilePath imageUtils) {
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableMinimalProduct convert(Product source, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException {

        ReadableMinimalProduct minimal = new ReadableMinimalProduct();
        this.merge(source, minimal, store, language);
        return minimal;
    }

    @Override
    public ReadableMinimalProduct merge(Product source, ReadableMinimalProduct destination, StoreMerchantId store,
                                        LanguageCode language) throws ProductPriceNotConvertibleException {
        applyDescription(source, destination, language);

        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        destination.setProductSpecifications(buildSpecifications(source));

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());
        destination.setSku(source.getSku());

        if (source.getDateAvailable() != null) {
            destination.setDateAvailable(source.getDateAvailable());
        }

        applyRating(source, destination);

        destination.setProductVirtual(source.isProductVirtual());
        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        applyPrice(source, destination, store);
        applyImages(source, destination, store);

        return destination;
    }

    private void applyDescription(Product source, ReadableMinimalProduct destination, LanguageCode language) {
        for (ProductDescription desc : source.getDescriptions()) {
            if (Objects.equals(desc.getLanguageCode(), language)) {
                destination.setDescription(this.description(desc));
                break;
            }
        }
    }

    private ProductSpecification buildSpecifications(Product source) {
        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        return specifications;
    }

    private void applyRating(Product source, ReadableMinimalProduct destination) {
        if (source.getProductReviewAvg() != null) {
            double avg = source.getProductReviewAvg().doubleValue();
            double rating = Math.round(avg * 2) / 2.0f;
            destination.setRating(rating);
        }
    }

    private void applyPrice(Product source, ReadableMinimalProduct destination, StoreMerchantId store)
            throws ProductPriceNotConvertibleException {
        try {
            FinalPriceCalc price = pricingService.calculateProductPrice(source);
            if (price != null) {
                destination.setFinalPrice(pricingService.getDisplayAmount(price.getFinalPrice(), store));
                destination.setPrice(price.getFinalPrice());
                destination.setOriginalPrice(pricingService.getDisplayAmount(price.getOriginalPrice(), store));
            }
        } catch (NoApplicableInventoryException e) {
            // A product with no priced inventory is a merchant configuration gap, not a broken conversion — but this
            // mapper has no way to render a price without one, so it reports the conversion it could not do and keeps
            // the 422 cause attached for the log.
            throw ProductPriceNotConvertibleException.of(e);
        }
    }

    private void applyImages(Product source, ReadableMinimalProduct destination, StoreMerchantId store) {
        Set<ProductImage> images = source.getImages();
        if (images == null || images.isEmpty()) {
            return;
        }
        List<ReadableImage> imageList = new ArrayList<>();
        String contextPath = imageUtils.getContextPath();

        for (ProductImage img : images) {
            ReadableImage prdImage = buildImage(source, store, contextPath, img);
            if (prdImage.isDefaultImage()) {
                destination.setImage(prdImage);
            }
            imageList.add(prdImage);
        }
        destination.setImages(imageList);
    }

    private ReadableImage buildImage(Product source, StoreMerchantId store, String contextPath, ProductImage img) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(img.getProductImage());
        prdImage.setDefaultImage(img.isDefaultImage());

        prdImage.setImageUrl(
                contextPath + imageUtils.buildProductImageUtils(store, source.getSku(), img.getProductImage()));
        prdImage.setId(img.getId());
        prdImage.setImageType(img.getImageType());
        if (img.getProductImageUrl() != null) {
            prdImage.setExternalUrl(img.getProductImageUrl());
        }
        if (img.getImageType() == 1 && img.getProductImageUrl() != null) { // video
            prdImage.setVideoUrl(img.getProductImageUrl());
        }
        return prdImage;
    }

    private ReadableDescription description(ProductDescription description) {
        ReadableDescription desc = new ReadableDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguageCode());
        desc.setFriendlyUrl(description.getSeUrl());
        desc.setTitle(description.getTitle());
        desc.setMetaDescription(description.getMetatagDescription());
        return desc;
    }

}
