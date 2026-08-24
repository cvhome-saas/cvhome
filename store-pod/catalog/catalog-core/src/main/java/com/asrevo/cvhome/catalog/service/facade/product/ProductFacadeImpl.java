package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;

@Service("productFacade")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeImpl implements ProductFacade {

    private final ProductService productService;

    private final ImageFilePath imageUtils;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ProductFacadeImpl(ProductService productService, ImageFilePath imageUtils,
                             ExternalMerchantStoreService externalStoreMerchantIdService) {
        this.productService = productService;
        this.imageUtils = imageUtils;
        this.externalMerchantStoreService = externalStoreMerchantIdService;
    }

    @Override
    public ReadableProductList getProductListsByCriteria(StoreMerchantId store, ProductCriteria criteria) {
        return null;
    }

    @Override
    public ReadableProductList getBaseProductListsByCriteria(StoreMerchantId merchantStore,
                                                             ProductCriteria searchCriteria) {
        return null;
    }

    @Override
    public ReadableProduct getProductBySeUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotConvertibleException {

        Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            return null;
        }

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator = new ReadableProductPopulator(imageUtils,
                externalMerchantStoreService);
        populator.populate(product, readableProduct, store, language);

        return readableProduct;
    }

    @Override
    public Product getProduct(Long id, StoreMerchantId store) {
        return productService.findOne(id, store);
    }

}
