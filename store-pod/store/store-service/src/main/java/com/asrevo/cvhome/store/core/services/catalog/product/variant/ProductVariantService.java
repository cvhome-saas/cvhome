package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductVariantService extends SalesManagerEntityService<Long, ProductVariant> {

    Optional<ProductVariant> getById(Long id, Long productId, MerchantStore store);

    List<ProductVariant> getByIds(List<Long> ids, MerchantStore store);

    Optional<ProductVariant> getById(Long id, MerchantStore store);

    Optional<ProductVariant> getBySku(String sku, Long productId, MerchantStore store, Language language);

    List<ProductVariant> getByProductId(MerchantStore store, Product product, Language language);


    Page<ProductVariant> getByProductId(MerchantStore store, Product product, Language language, int page, int count);


    boolean exist(String sku, Long productId);

    ProductVariant saveProductVariant(ProductVariant variant) throws ServiceException;


}
