package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ProductTypeFacade {

    ReadableProductTypeList getByMerchant(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ReadableProductType get(StoreMerchantId store, Long id, LanguageCode language);

    ReadableProductType get(StoreMerchantId store, String code, LanguageCode language);

    Long save(PersistableProductType type, StoreMerchantId store, LanguageCode language);

    void update(PersistableProductType type, Long id, StoreMerchantId store, LanguageCode language);

    void delete(Long id, StoreMerchantId store, LanguageCode language);

    boolean exists(String code, StoreMerchantId store, LanguageCode language);

}
