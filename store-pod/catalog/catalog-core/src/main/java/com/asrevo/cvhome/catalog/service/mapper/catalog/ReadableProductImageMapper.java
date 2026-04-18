package com.asrevo.cvhome.catalog.service.mapper.catalog;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableProductImageMapper implements Mapper<ProductImage, ReadableImage> {

    private final ImageFilePath imageUtils;

    public ReadableProductImageMapper(ImageFilePath imageUtils) {
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableImage convert(ProductImage source, StoreMerchantId store, LanguageCode language) {
        ReadableImage destination = new ReadableImage();
        return merge(source, destination, store, language);
    }

    @Override
    public ReadableImage merge(ProductImage source, ReadableImage destination, StoreMerchantId store,
                               LanguageCode language) {

        String contextPath = imageUtils.getContextPath();

        destination.setImageName(source.getProductImage());
        destination.setDefaultImage(source.isDefaultImage());
        destination.setOrder(source.getSortOrder() != null ? source.getSortOrder() : 0);

        if (source.getImageType() == 1 && source.getProductImageUrl() != null) {
            destination.setImageUrl(source.getProductImageUrl());
        } else {
            StringBuilder imgPath = new StringBuilder();
            imgPath.append(contextPath)
                    .append(imageUtils.buildProductImageUtils(store, source.getProduct().getSku(),
                            source.getProductImage()));
            destination.setImageUrl(imgPath.toString());
        }
        destination.setId(source.getId());
        destination.setImageType(source.getImageType());
        if (source.getProductImageUrl() != null) {
            destination.setExternalUrl(source.getProductImageUrl());
        }
        if (source.getImageType() == 1 && source.getProductImageUrl() != null) { // video
            destination.setVideoUrl(source.getProductImageUrl());
        }

        return destination;
    }

}
