package com.asrevo.cvhome.store.service.facade.zone;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.references.ReadableZone;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.service.populator.references.ReadableZonePopulator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ZoneFacadeImpl implements ZoneFacade {

    private final ZoneService zoneService;

    public ZoneFacadeImpl(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Override
    public List<ReadableZone> getZones(
            String countryCode, Language language, MerchantStore merchantStore) {
        List<Zone> listZones = getListZones(countryCode, language);
        if (listZones.isEmpty()) {
            return Collections.emptyList();
            // throw new ResourceNotFoundException("No zones found");
        }
        return listZones.stream()
                .map(zone -> convertToReadableZone(zone, language, merchantStore))
                .collect(Collectors.toList());
    }

    private ReadableZone convertToReadableZone(
            Zone zone, Language language, MerchantStore merchantStore) {
        try {
            ReadableZonePopulator populator = new ReadableZonePopulator();
            return populator.populate(zone, new ReadableZone(), merchantStore, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    private List<Zone> getListZones(String countryCode, Language language) {
        try {
            return zoneService.getZones(countryCode, language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
