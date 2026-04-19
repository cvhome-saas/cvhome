package com.asrevo.cvhome.checkout.services.reference.zone;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ZoneService extends SalesManagerEntityService<ZoneCode, Zone> {

    Zone getByCode(ZoneCode code);

    Map<ZoneCode, Zone> getZones(LanguageCode language);

    List<Zone> getZones(CountryIsoCode countryCode, LanguageCode language);

}
