package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.ReadableProductOptionSet;

import java.util.List;

public interface ProductOptionSetFacade {


    ReadableProductOptionSet get(Long id, MerchantStore store, Language language);

    boolean exists(String code, MerchantStore store);

    List<ReadableProductOptionSet> list(MerchantStore store, Language language);

    List<ReadableProductOptionSet> list(MerchantStore store, Language language, String type);

    void create(PersistableProductOptionSet optionSet, MerchantStore store, Language language);

    void update(Long id, PersistableProductOptionSet optionSet, MerchantStore store, Language language);

    void delete(Long id, MerchantStore store);


}
