package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionSetException;
import com.asrevo.cvhome.catalog.errors.ProductOptionSetNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ProductOptionSetFacade {

    ReadableProductOptionSet get(Long id, StoreMerchantId store, LanguageCode language)
            throws ProductOptionSetNotFoundException;

    boolean exists(String code, StoreMerchantId store);

    List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language);

    List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language, String type)
            throws ProductTypeNotFoundException;

    void create(PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language)
            throws DuplicateProductOptionSetException, ServiceException;

    void update(Long id, PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language)
            throws ProductOptionSetNotFoundException;

    void delete(Long id, StoreMerchantId store)
            throws ProductOptionSetNotFoundException, ServiceException;

}
