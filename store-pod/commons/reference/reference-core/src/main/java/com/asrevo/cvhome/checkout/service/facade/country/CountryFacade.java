package com.asrevo.cvhome.checkout.service.facade.country;

import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;

public interface CountryFacade {

    List<ReadableCountry> getListCountryZones(LanguageCode language, StoreMerchantId merchantStore);

}
