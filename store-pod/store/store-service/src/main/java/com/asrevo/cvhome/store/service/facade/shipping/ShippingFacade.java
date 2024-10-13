package com.asrevo.cvhome.store.service.facade.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.references.ReadableCountry;
import java.util.List;

public interface ShippingFacade {

    List<ReadableCountry> shipToCountry(MerchantStore store, Language lanuage);
}
