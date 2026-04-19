package com.asrevo.cvhome.store.utils;

import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;

public interface ImageFilePath {

    /**
     * Context path configured in shopizer-properties.xml
     */
    String getContextPath();

    String getBasePath(StoreMerchantId store);

    /**
     * Builds a static content image file path that can be used by image servlet utility
     * for getting the physical image
     */
    default String buildStaticImageUtils(StoreMerchantId store, String imageName) {
        StringBuilder imgName = new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(FileContentType.IMAGE.name())
                .append(Constants.SLASH);
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();
    }

    /**
     * Builds a default product image file path that can be used by image servlet utility
     * for getting the physical image
     */
    default String buildProductImageUtils(StoreMerchantId store, String sku, String imageName) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.PRODUCTS_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(sku)
                .append(Constants.SLASH)
                .append(Constants.SMALL_IMAGE)
                .append(Constants.SLASH)
                .append(imageName)
                .toString();
    }

    /**
     * Builds a merchant store logo path
     */
    default String buildStoreLogoFilePath(StoreMerchantId store, String storeLogo) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(FileContentType.LOGO)
                .append(Constants.SLASH)
                .append(storeLogo)
                .toString();
    }

    /**
     * Builds a merchant store banner path
     */
    default String buildStoreBannerFilePath(StoreMerchantId store, String storeBanner) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(FileContentType.BANNER)
                .append(Constants.SLASH)
                .append(storeBanner)
                .toString();
    }

    default String buildStoreSliderFilePath(StoreMerchantId store, String sliderImage) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(FileContentType.SLIDER)
                .append(Constants.SLASH)
                .append(sliderImage)
                .toString();
    }

    default String buildProductPropertyImageUtils(StoreMerchantId store, String imageName) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(FileContentType.PROPERTY)
                .append(Constants.SLASH)
                .append(imageName)
                .toString();
    }

    default String buildCustomTypeImageUtils(StoreMerchantId store, String imageName, FileContentType type) {
        return new StringBuilder().append(getBasePath(store))
                .append(Constants.FILES_URI)
                .append(Constants.SLASH)
                .append(store.getId())
                .append(Constants.SLASH)
                .append(type)
                .append(Constants.SLASH)
                .append(imageName)
                .toString();
    }

}
