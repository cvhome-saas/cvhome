package com.asrevo.cvhome.order.service.facade.country;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;
import java.util.List;

public interface CountryFacade {
    List<ReadableCountry> getListCountryZones(LanguageCode language, StoreMerchantId merchantStore);
}
