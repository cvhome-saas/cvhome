package com.asrevo.cvhome.catalog.services.product.variant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductVariantService extends SalesManagerEntityService<Long, ProductVariant> {

    Optional<ProductVariant> getById(Long id, Long productId, StoreMerchantId store);

    List<ProductVariant> getByIds(List<Long> ids, StoreMerchantId store);

    Optional<ProductVariant> getById(Long id, StoreMerchantId store);

    List<ProductVariant> getByProductId(StoreMerchantId store, Product product, LanguageCode language);

    Page<ProductVariant> getByProductId(StoreMerchantId store, Product product, LanguageCode language,
                                        Pageable pageable);

    boolean exist(String sku, Long productId);

    ProductVariant saveProductVariant(ProductVariant variant);

}
