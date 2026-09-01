package com.asrevo.cvhome.catalog.services.option;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionException;
import com.asrevo.cvhome.catalog.errors.ProductOptionInUseException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * The store's option vocabulary (Color, Size, …) with its values — written as whole documents; products reference
 * options by assignment, so deletion is refused while anything still points at the option.
 */
public interface ProductOptionService {

    ReadableEntityList<ReadableProductOption> list(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ReadableProductOption get(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductOptionNotFoundException;

    boolean exists(StoreMerchantId store, String code);

    Long create(StoreMerchantId store, PersistableProductOption source) throws DuplicateProductOptionException;

    void update(StoreMerchantId store, Long id, PersistableProductOption source)
            throws ProductOptionNotFoundException, DuplicateProductOptionException, ProductOptionInUseException;

    void delete(StoreMerchantId store, Long id) throws ProductOptionNotFoundException, ProductOptionInUseException;
}
