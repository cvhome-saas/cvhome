package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.DuplicateProductTypeException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductTypeFacade {

    ReadableProductTypeList getByMerchant(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ReadableProductType get(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductTypeNotFoundException;

    ReadableProductType get(StoreMerchantId store, String code, LanguageCode language)
            throws ProductTypeNotFoundException;

    Long save(PersistableProductType type, StoreMerchantId store, LanguageCode language)
            throws DuplicateProductTypeException;

    void update(PersistableProductType type, Long id, StoreMerchantId store, LanguageCode language)
            throws ProductTypeNotFoundException;

    void delete(Long id, StoreMerchantId store, LanguageCode language)
            throws ProductTypeNotFoundException;

    boolean exists(String code, StoreMerchantId store, LanguageCode language);

}
