package com.asrevo.cvhome.catalog.repositories.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.ProductList;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface ProductRepositoryCustom {

    ProductList listByStore(StoreMerchantId store, LanguageCode language, ProductCriteria criteria);

    Product getByFriendlyUrl(StoreMerchantId store, String seUrl, Locale locale);

    List<Product> getProductsListByCategories(@SuppressWarnings("rawtypes") Set categoryIds);

    List<Product> listByStore(StoreMerchantId store);

    Product getById(Long productId, StoreMerchantId merchant);

    Product getById(Long productId, StoreMerchantId store, LanguageCode language);

    Product getMinimalProductById(Long productId, StoreMerchantId merchant, LanguageCode language);
}
