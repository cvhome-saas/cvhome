package com.asrevo.cvhome.checkout.services.reference.zone;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.checkout.repositories.reference.zone.ZoneRepository;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service("zoneService")
@Slf4j
public class ZoneServiceImpl extends SalesManagerEntityServiceImpl<ZoneCode, Zone> implements ZoneService {

    private final ZoneRepository zoneRepository;

    @Autowired
    public ZoneServiceImpl(ZoneRepository zoneRepository) {
        super(zoneRepository);
        this.zoneRepository = zoneRepository;
    }

    @Override
    @Cacheable("zoneByCode")
    public Zone getByCode(ZoneCode code) {
        return zoneRepository.findByCode(code);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Zone> getZones(CountryIsoCode countryCode, LanguageCode language) {
        return zoneRepository.listByLanguageAndCountry(countryCode, language);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<ZoneCode, Zone> getZones(LanguageCode language) {
        return zoneRepository.listByLanguage(language).stream().collect(Collectors.toMap(Zone::getCode, z -> z));
    }

}
