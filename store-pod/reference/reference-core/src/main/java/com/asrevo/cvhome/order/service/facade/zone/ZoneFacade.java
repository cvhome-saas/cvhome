package com.asrevo.cvhome.order.service.facade.zone;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableZone;
import java.util.List;

public interface ZoneFacade {

    List<ReadableZone> getZones(
            CountryIsoCode countryCode, LanguageCode language, StoreMerchantId merchantStore);
}
