package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.ProductList;
import com.asrevo.cvhome.catalog.model.product.ProductAvailabilityStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService extends SalesManagerEntityService<Long, Product> {

    Optional<Product> retrieveById(Long id, StoreMerchantId store);

    List<Product> getProducts(List<Long> categoryIds);

    /**
     * The method to be used
     */
    Product saveProduct(Product product) throws ServiceException;

    ProductList listByStore(StoreMerchantId store, LanguageCode language, ProductCriteria criteria);

    boolean exists(String sku, StoreMerchantId store);

    /**
     * List using Page interface in order to unify all page requests (since 2.16.0)
     */
    Page<Product> listByStore(
            StoreMerchantId store,
            LanguageCode language,
            ProductCriteria criteria,
            int page,
            int count);

    List<Product> listByStore(StoreMerchantId store);

    Product getBySeUrl(StoreMerchantId store, String seUrl, Locale locale);

    /**
     * Product and or product variant
     */
    Product getBySku(String productCode, StoreMerchantId merchant, LanguageCode language)
            throws ServiceException;

    Product getBySku(String productCode, StoreMerchantId merchant) throws ServiceException;

    /**
     * Find a product for a specific merchant
     */
    Product findOne(Long id, StoreMerchantId merchant);

    FinalPrice getProductPrice(StoreMerchantId merchant, String sku) throws ServiceException;

    ReadableProduct getFullProduct(StoreMerchantId store, String productSku, LanguageCode language);

    ProductAvailabilityStatus productQuantityUpdate(
            StoreMerchantId store, ProductQuantityUpdate productQuantityUpdate)
            throws ServiceException;

    ReadableMinimalProduct getMinimalProduct(
            StoreMerchantId store, String productSku, LanguageCode language);

    ReadableProductAvailability getProductAvailability(StoreMerchantId store, String sku);
}
