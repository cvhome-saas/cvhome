package com.asrevo.cvhome.store.core.repositories.catalog.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.tax.taxclass.TaxClass;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface ProductRepositoryCustom {

    ProductList listByStore(MerchantStore store, Language language, ProductCriteria criteria);

    Product getProductWithOnlyMerchantStoreById(Long productId);

    Product getByFriendlyUrl(MerchantStore store, String seUrl, Locale locale);

    List<Product> getProductsListByCategories(@SuppressWarnings("rawtypes") Set categoryIds);

    List<Product> getProductsListByCategories(Set<Long> categoryIds, Language language);

    List<Product> getProductsListByIds(Set<Long> productIds);

    List<Product> listByTaxClass(TaxClass taxClass);

    List<Product> listByStore(MerchantStore store);

    Product getProductForLocale(long productId, Language language, Locale locale);

    Product getById(Long productId);

    Product getById(Long productId, MerchantStore merchant);

    /**
     * Get product by code
     *
     * @deprecated This method is no longer acceptable to get product by code.
     * <p> Use  instead.
     */
    @Deprecated
    Product getByCode(String productCode, Language language);

    /**
     * Get product by code
     *
     * @deprecated This method is no longer acceptable to get product by code.
     * <p> Use  instead.
     */
    @Deprecated
    Product getByCode(String productCode, MerchantStore store);

    Product getById(Long productId, MerchantStore store, Language language);

    List<Product> getProductsForLocale(
            MerchantStore store, Set<Long> categoryIds, Language language, Locale locale);
}
