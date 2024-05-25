package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import org.apache.commons.lang3.StringUtils;


public interface AbstractimageFilePath extends ImageFilePath {


    String getBasePath(MerchantStore store);


    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    default String buildStaticImageUtils(MerchantStore store, String imageName) {
        StringBuilder imgName = new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).append(FileContentType.IMAGE.name()).append(Constants.SLASH);
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();

    }

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image by specifying the image type
     *
     */
    default String buildStaticImageUtils(MerchantStore store, String type, String imageName) {
        StringBuilder imgName = new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).append(type).append(Constants.SLASH);
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();

    }

    /**
     * Builds a manufacturer image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    default String buildManufacturerImageUtils(MerchantStore store, Manufacturer manufacturer, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).
                append(FileContentType.MANUFACTURER.name()).append(Constants.SLASH)
                .append(manufacturer.getId()).append(Constants.SLASH)
                .append(imageName).toString();
    }

    /**
     * Builds a product image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    default String buildProductImageUtils(MerchantStore store, Product product, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.PRODUCTS_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH)
                .append(product.getSku()).append(Constants.SLASH).append(Constants.SMALL_IMAGE).append(Constants.SLASH).append(imageName).toString();
    }

    /**
     * Builds a default product image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    default String buildProductImageUtils(MerchantStore store, String sku, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.PRODUCTS_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH)
                .append(sku).append(Constants.SLASH).append(Constants.SMALL_IMAGE).append(Constants.SLASH).append(imageName).toString();
    }

    /**
     * Builds a large product image file path that can be used by the image servlet
     *
     */
    default String buildLargeProductImageUtils(MerchantStore store, String sku, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH)
                .append(sku).append(Constants.SLASH).append(Constants.SMALL_IMAGE).append(Constants.SLASH).append(imageName).toString();
    }


    /**
     * Builds a merchant store logo path
     *
     */
    default String buildStoreLogoFilePath(MerchantStore store) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).append(FileContentType.LOGO).append(Constants.SLASH)
                .append(store.getStoreLogo()).toString();
    }

    /**
     * Builds a merchant store banner path
     *
     */
    default String buildStoreBannerFilePath(MerchantStore store) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).append(FileContentType.BANNER).append(Constants.SLASH)
                .append(store.getStoreBanner()).toString();
    }

    /**
     * Builds product property image url path
     *
     */
    default String buildProductPropertyImageFilePath(MerchantStore store, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH).append(FileContentType.PROPERTY).append(Constants.SLASH)
                .append(imageName).toString();
    }

    default String buildProductPropertyImageUtils(MerchantStore store, String imageName) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append("/").append(FileContentType.PROPERTY).append("/")
                .append(imageName).toString();
    }

    default String buildCustomTypeImageUtils(MerchantStore store, String imageName, FileContentType type) {
        return new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append("/").append(type).append("/")
                .append(imageName).toString();
    }

    /**
     * Builds static file url path
     *
     */
    default String buildStaticContentFilePath(MerchantStore store, String fileName) {
        StringBuilder sb = new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH);
        if (!StringUtils.isBlank(fileName)) {
            sb.append(fileName);
        }
        return sb.toString();
    }


}
