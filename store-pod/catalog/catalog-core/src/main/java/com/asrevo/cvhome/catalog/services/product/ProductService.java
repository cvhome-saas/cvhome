package com.asrevo.cvhome.catalog.services.product;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductService extends SalesManagerEntityService<Long, Product> {

    Optional<Product> retrieveById(Long id, StoreMerchantId store);

    List<Product> getProducts(List<Long> categoryIds);

    /**
     * The method to be used
     */
    /**
     * @throws ProductNotPersistedException the write did not complete
     */
    Product saveProduct(Product product) throws ProductNotPersistedException;

    boolean exists(String sku, StoreMerchantId store);

    List<Product> listByStore(StoreMerchantId store);

    Product getBySeUrl(StoreMerchantId store, String seUrl, Locale locale);

    /**
     * Product and or product variant
     */
    /**
     * @throws ProductNotFoundException no product carries that sku in this store
     */
    Product getBySku(String productCode, StoreMerchantId merchant, LanguageCode language) throws ProductNotFoundException;

    /**
     * @throws ProductNotFoundException no product carries that sku in this store
     */
    Product getBySku(String productCode, StoreMerchantId merchant) throws ProductNotFoundException;

    /**
     * Find a product for a specific merchant
     */
    Product findOne(Long id, StoreMerchantId merchant);

    Page<Product> findAll(ProductCriteria criteria, StoreMerchantId store);

    ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode lang);

}
