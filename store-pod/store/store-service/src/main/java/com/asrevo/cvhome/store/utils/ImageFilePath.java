package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;

public interface ImageFilePath {

    /**
     * Context path configured in shopizer-properties.xml
     *
     */
    String getContextPath();

    String getBasePath(MerchantStore store);

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    String buildStaticImageUtils(MerchantStore store, String imageName);

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image by specifying the image type
     *
     */
    String buildStaticImageUtils(MerchantStore store, String type, String imageName);

    /**
     * Builds a manufacturer image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    String buildManufacturerImageUtils(
            MerchantStore store, Manufacturer manufacturer, String imageName);

    /**
     * Builds a product image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    String buildProductImageUtils(MerchantStore store, Product product, String imageName);

    /**
     * Builds a default product image file path that can be used by image servlet
     * utility for getting the physical image
     *
     */
    String buildProductImageUtils(MerchantStore store, String sku, String imageName);

    /**
     * Builds a large product image file path that can be used by the image servlet
     *
     */
    String buildLargeProductImageUtils(MerchantStore store, String sku, String imageName);

    /**
     * Builds a merchant store logo path
     *
     */
    String buildStoreLogoFilePath(MerchantStore store);

    /**
     * Builds a merchant store logo path
     *
     */
    String buildStoreBannerFilePath(MerchantStore store);

    /**
     * Builds product property image url path
     *
     */
    String buildProductPropertyImageUtils(MerchantStore store, String imageName);

    /**
     * A custom file type image handler
     *
     */
    String buildCustomTypeImageUtils(MerchantStore store, String imageName, FileContentType type);

    /**
     * Builds static file path
     *
     */
    String buildStaticContentFilePath(MerchantStore store, String fileName);
}
