package com.asrevo.cvhome.store.service.facade.zone;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.references.ReadableZone;
import java.util.List;

public interface ZoneFacade {

    List<ReadableZone> getZones(String countryCode, Language language, MerchantStore merchantStore);
}
