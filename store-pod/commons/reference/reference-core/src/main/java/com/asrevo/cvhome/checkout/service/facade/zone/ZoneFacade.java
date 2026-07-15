package com.asrevo.cvhome.checkout.service.facade.zone;

import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableZone;

public interface ZoneFacade {

    List<ReadableZone> getZones(CountryIsoCode countryCode, LanguageCode language, StoreMerchantId merchantStore);

}
