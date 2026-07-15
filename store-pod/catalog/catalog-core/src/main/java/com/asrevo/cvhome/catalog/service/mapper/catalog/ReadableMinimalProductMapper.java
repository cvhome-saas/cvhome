package com.asrevo.cvhome.catalog.service.mapper.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;
import com.asrevo.cvhome.commons.domain.LanguageCode;
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
    public ReadableMinimalProduct convert(Product source, StoreMerchantId store, LanguageCode language) {

        ReadableMinimalProduct minimal = new ReadableMinimalProduct();
        this.merge(source, minimal, store, language);
        return minimal;
    }

    @Override
    public ReadableMinimalProduct merge(Product source, ReadableMinimalProduct destination, StoreMerchantId store,
                                        LanguageCode language) {
        for (ProductDescription desc : source.getDescriptions()) {
            if (Objects.equals(desc.getLanguageCode(), language)) {
                destination.setDescription(this.description(desc));
                break;
            }
        }

        destination.setId(source.getId());
        destination.setAvailable(source.isAvailable());
        destination.setProductShipeable(source.isProductShipeable());

        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        destination.setProductSpecifications(specifications);

        destination.setPreOrder(source.isPreOrder());
        destination.setRefSku(source.getRefSku());
        destination.setSortOrder(source.getSortOrder());
        destination.setSku(source.getSku());

        if (source.getDateAvailable() != null) {
            destination.setDateAvailable(source.getDateAvailable());
        }

        if (source.getProductReviewAvg() != null) {
            double avg = source.getProductReviewAvg().doubleValue();
            double rating = Math.round(avg * 2) / 2.0f;
            destination.setRating(rating);
        }

        destination.setProductVirtual(source.isProductVirtual());
        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount());
        }

        // price

        try {
            FinalPriceCalc price = pricingService.calculateProductPrice(source);
            if (price != null) {

                destination.setFinalPrice(pricingService.getDisplayAmount(price.getFinalPrice(), store));
                destination.setPrice(price.getFinalPrice());
                destination.setOriginalPrice(pricingService.getDisplayAmount(price.getOriginalPrice(), store));
            }
        } catch (ServiceException e) {
            throw new ConversionRuntimeException("An error occured during price calculation", e);
        }

        // image
        Set<ProductImage> images = source.getImages();
        if (images != null && !images.isEmpty()) {
            List<ReadableImage> imageList = new ArrayList<>();

            String contextPath = imageUtils.getContextPath();

            for (ProductImage img : images) {
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

                if (prdImage.isDefaultImage()) {
                    destination.setImage(prdImage);
                }

                imageList.add(prdImage);
            }
            destination.setImages(imageList);
        }

        return destination;
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
