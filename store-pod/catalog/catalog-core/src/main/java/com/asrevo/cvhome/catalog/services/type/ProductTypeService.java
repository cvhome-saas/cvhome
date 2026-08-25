package com.asrevo.cvhome.catalog.services.type;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.DuplicateProductTypeException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * Product types. Every read answers every language: these are console-only records.
 */
public interface ProductTypeService {

    ReadableEntityList<ReadableProductType> list(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ReadableProductType get(StoreMerchantId store, Long id, LanguageCode language) throws ProductTypeNotFoundException;

    boolean exists(StoreMerchantId store, String code);

    /**
     * @throws DuplicateProductTypeException the code is already taken in this store
     */
    Long create(StoreMerchantId store, PersistableProductType type) throws DuplicateProductTypeException;

    /**
     * Updates everything but the code, which is the type's identity once products point at it.
     */
    void update(StoreMerchantId store, Long id, PersistableProductType type) throws ProductTypeNotFoundException;

    void delete(StoreMerchantId store, Long id) throws ProductTypeNotFoundException;
}
