package com.asrevo.cvhome.catalog.services.product.variation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductVariationService extends SalesManagerEntityService<Long, ProductVariation> {

    void saveOrUpdate(ProductVariation entity);

    Optional<ProductVariation> getById(StoreMerchantId store, Long id, LanguageCode lang);

    Optional<ProductVariation> getById(StoreMerchantId store, Long id);

    Optional<ProductVariation> getByCode(StoreMerchantId store, String code);

    Page<ProductVariation> getByMerchant(StoreMerchantId store, LanguageCode language, String code, Pageable pageable);

    List<ProductVariation> getByIds(List<Long> ids, StoreMerchantId store);

}
