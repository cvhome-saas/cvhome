package com.asrevo.cvhome.checkout.service.facade.zone;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.checkout.service.populator.references.ReadableZonePopulator;
import com.asrevo.cvhome.checkout.services.reference.zone.ZoneService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableZone;

@Service
public class ZoneFacadeImpl implements ZoneFacade {

    private final ZoneService zoneService;

    public ZoneFacadeImpl(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Override
    public List<ReadableZone> getZones(CountryIsoCode countryCode, LanguageCode language,
                                       StoreMerchantId merchantStore) {
        List<Zone> listZones = getListZones(countryCode, language);
        if (listZones.isEmpty()) {
            return Collections.emptyList();
        }
        return listZones.stream()
                .map(zone -> convertToReadableZone(zone, language, merchantStore))
                .toList();
    }

    private ReadableZone convertToReadableZone(Zone zone, LanguageCode language, StoreMerchantId merchantStore) {
        ReadableZonePopulator populator = new ReadableZonePopulator();
        return populator.populate(zone, new ReadableZone(), merchantStore, language);
    }

    private List<Zone> getListZones(CountryIsoCode countryCode, LanguageCode language) {
        return zoneService.getZones(countryCode, language);
    }

}
