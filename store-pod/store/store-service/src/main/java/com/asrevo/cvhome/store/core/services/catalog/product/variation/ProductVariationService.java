package com.asrevo.cvhome.store.core.services.catalog.product.variation;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductVariationService extends SalesManagerEntityService<Long, ProductVariation> {


    void saveOrUpdate(ProductVariation entity) throws ServiceException;

    Optional<ProductVariation> getById(MerchantStore store, Long id, Language lang);

    Optional<ProductVariation> getById(MerchantStore store, Long id);

    Optional<ProductVariation> getByCode(MerchantStore store, String code);

    Page<ProductVariation> getByMerchant(MerchantStore store, Language language, String code, int page, int count);

    List<ProductVariation> getByIds(List<Long> ids, MerchantStore store);

}
