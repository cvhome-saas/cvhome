package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ProductOptionSetFacade {

    ReadableProductOptionSet get(Long id, StoreMerchantId store, LanguageCode language);

    boolean exists(String code, StoreMerchantId store);

    List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language);

    List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language, String type);

    void create(PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language);

    void update(Long id, PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language);

    void delete(Long id, StoreMerchantId store);

}
