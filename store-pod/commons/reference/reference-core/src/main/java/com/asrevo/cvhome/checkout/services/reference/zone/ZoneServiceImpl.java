package com.asrevo.cvhome.checkout.services.reference.zone;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.checkout.repositories.reference.zone.ZoneRepository;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("zoneService")
@Slf4j
public class ZoneServiceImpl extends SalesManagerEntityServiceImpl<ZoneCode, Zone> implements ZoneService {

	private static final String ZONE_CACHE_PREFIX = "ZONES_";

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

		Assert.notNull(countryCode, "countryCode cannot be null");
		Assert.notNull(language, "Language cannot be null");

		return zoneRepository.listByLanguageAndCountry(countryCode, language);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<ZoneCode, Zone> getZones(LanguageCode language) {
		return zoneRepository.listByLanguage(language).stream().collect(Collectors.toMap(Zone::getCode, z -> z));
	}

}
