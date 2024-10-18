package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.springframework.stereotype.Component;

@Component
public class ReadableProductImageMapper implements Mapper<ProductImage, ReadableImage> {

    private final ImageFilePath imageUtils;

    public ReadableProductImageMapper(ImageFilePath imageUtils) {
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableImage convert(ProductImage source, MerchantStore store, Language language) {
        ReadableImage destination = new ReadableImage();
        return merge(source, destination, store, language);
    }

    @Override
    public ReadableImage merge(
            ProductImage source,
            ReadableImage destination,
            MerchantStore store,
            Language language) {

        String contextPath = imageUtils.getContextPath();

        destination.setImageName(source.getProductImage());
        destination.setDefaultImage(source.isDefaultImage());
        destination.setOrder(source.getSortOrder() != null ? source.getSortOrder() : 0);

        if (source.getImageType() == 1 && source.getProductImageUrl() != null) {
            destination.setImageUrl(source.getProductImageUrl());
        } else {
            StringBuilder imgPath = new StringBuilder();
            imgPath.append(contextPath)
                    .append(
                            imageUtils.buildProductImageUtils(
                                    store, source.getProduct().getSku(), source.getProductImage()));
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
