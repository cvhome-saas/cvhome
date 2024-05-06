package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableMinimalProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductSpecification;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ReadableMinimalProductMapper implements Mapper<Product, ReadableMinimalProduct> {

    private final PricingService pricingService;


    private final ImageFilePath imageUtils;

    public ReadableMinimalProductMapper(PricingService pricingService, ImageFilePath imageUtils) {
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableMinimalProduct convert(Product source, MerchantStore store, Language language) {
        // TODO Auto-generated method stub
        ReadableMinimalProduct minimal = new ReadableMinimalProduct();
        return this.merge(source, minimal, store, language);
    }

    @Override
    public ReadableMinimalProduct merge(Product source, ReadableMinimalProduct destination, MerchantStore store,
                                        Language language) {
        Assert.notNull(source, "Product cannot be null");
        Assert.notNull(destination, "ReadableMinimalProduct cannot be null");


        for (ProductDescription desc : source.getDescriptions()) {
            if (language != null && desc.getLanguage() != null
                    && desc.getLanguage().getId().intValue() == language.getId().intValue()) {
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
            destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
        }

        if (source.getProductReviewAvg() != null) {
            double avg = source.getProductReviewAvg().doubleValue();
            double rating = Math.round(avg * 2) / 2.0f;
            destination.setRating(rating);
        }

        destination.setProductVirtual(source.isProductVirtual());
        if (source.getProductReviewCount() != null) {
            destination.setRatingCount(source.getProductReviewCount().intValue());
        }

        //price

        try {
            FinalPrice price = pricingService.calculateProductPrice(source);
            if (price != null) {

                destination.setFinalPrice(pricingService.getDisplayAmount(price.getFinalPrice(), store));
                destination.setPrice(price.getFinalPrice());
                destination.setOriginalPrice(pricingService.getDisplayAmount(price.getOriginalPrice(), store));

            }
        } catch (ServiceException e) {
            throw new ConversionRuntimeException("An error occured during price calculation", e);
        }


        //image
        Set<ProductImage> images = source.getImages();
        if (images != null && !images.isEmpty()) {
            List<ReadableImage> imageList = new ArrayList<>();

            String contextPath = imageUtils.getContextPath();

            for (ProductImage img : images) {
                ReadableImage prdImage = new ReadableImage();
                prdImage.setImageName(img.getProductImage());
                prdImage.setDefaultImage(img.isDefaultImage());

                prdImage.setImageUrl(contextPath + imageUtils.buildProductImageUtils(store, source.getSku(), img.getProductImage()));
                prdImage.setId(img.getId());
                prdImage.setImageType(img.getImageType());
                if (img.getProductImageUrl() != null) {
                    prdImage.setExternalUrl(img.getProductImageUrl());
                }
                if (img.getImageType() == 1 && img.getProductImageUrl() != null) {//video
                    prdImage.setVideoUrl(img.getProductImageUrl());
                }

                if (prdImage.isDefaultImage()) {
                    destination.setImage(prdImage);
                }

                imageList.add(prdImage);
            }
            destination
                    .setImages(imageList);
        }


        return null;
    }

    private ReadableDescription description(ProductDescription description) {
        ReadableDescription desc = new ReadableDescription();
        desc.setDescription(description.getDescription());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setLanguage(description.getLanguage().getCode());
        desc.setFriendlyUrl(description.getSeUrl());
        desc.setTitle(description.getTitle());
        desc.setMetaDescription(description.getMetatagDescription());
        return desc;
    }

}
