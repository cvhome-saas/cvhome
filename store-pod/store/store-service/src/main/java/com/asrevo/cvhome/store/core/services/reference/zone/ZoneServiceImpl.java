package com.asrevo.cvhome.store.core.services.reference.zone;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.reference.zone.ZoneRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.utils.CacheUtils;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("zoneService")
public class ZoneServiceImpl extends SalesManagerEntityServiceImpl<Long, Zone> implements
        ZoneService {

    private final static String ZONE_CACHE_PREFIX = "ZONES_";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneServiceImpl.class);
    private ZoneRepository zoneRepository;
    @Autowired
    private CacheUtils cache;

    @Autowired
    public ZoneServiceImpl(ZoneRepository zoneRepository) {
        super(zoneRepository);
        this.zoneRepository = zoneRepository;
    }

    @Override
    @Cacheable("zoneByCode")
    public Zone getByCode(String code) {
        return zoneRepository.findByCode(code);
    }

    @Override
    public void addDescription(Zone zone, ZoneDescription description) throws ServiceException {
        if (zone.getDescriptions() != null) {
            if (!zone.getDescriptions().contains(description)) {
                zone.getDescriptions().add(description);
                update(zone);
            }
        } else {
            List<ZoneDescription> descriptions = new ArrayList<ZoneDescription>();
            descriptions.add(description);
            zone.setDescriptions(descriptions);
            update(zone);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Zone> getZones(Country country, Language language) throws ServiceException {

        //Assert.notNull(country,"Country cannot be null");
        Assert.notNull(language, "Language cannot be null");

        List<Zone> zones = null;
        try {

            String countryCode = Constants.DEFAULT_COUNTRY;
            if (country != null) {
                countryCode = country.getIsoCode();
            }

            String cacheKey = ZONE_CACHE_PREFIX + countryCode + Constants.UNDERSCORE + language.getCode();

            zones = (List<Zone>) cache.getFromCache(cacheKey);


            if (zones == null) {

                zones = zoneRepository.listByLanguageAndCountry(countryCode, language.getId());

                //set names
                for (Zone zone : zones) {
                    ZoneDescription description = zone.getDescriptions().get(0);
                    zone.setName(description.getName());

                }
                cache.putInCache(zones, cacheKey);
            }

        } catch (Exception e) {
            LOGGER.error("getZones()", e);
        }
        return zones;


    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Zone> getZones(String countryCode, Language language) throws ServiceException {

        Assert.notNull(countryCode, "countryCode cannot be null");
        Assert.notNull(language, "Language cannot be null");

        List<Zone> zones = null;
        try {


            String cacheKey = ZONE_CACHE_PREFIX + countryCode + Constants.UNDERSCORE + language.getCode();

            zones = (List<Zone>) cache.getFromCache(cacheKey);


            if (zones == null) {

                zones = zoneRepository.listByLanguageAndCountry(countryCode, language.getId());

                //set names
                for (Zone zone : zones) {
                    ZoneDescription description = zone.getDescriptions().get(0);
                    zone.setName(description.getName());

                }
                cache.putInCache(zones, cacheKey);
            }

        } catch (Exception e) {
            LOGGER.error("getZones()", e);
        }
        return zones;


    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Zone> getZones(Language language) throws ServiceException {

        Map<String, Zone> zones = null;
        try {

            String cacheKey = ZONE_CACHE_PREFIX + language.getCode();

            zones = (Map<String, Zone>) cache.getFromCache(cacheKey);


            if (zones == null) {
                zones = new HashMap<String, Zone>();
                List<Zone> zns = zoneRepository.listByLanguage(language.getId());

                //set names
                for (Zone zone : zns) {
                    ZoneDescription description = zone.getDescriptions().get(0);
                    zone.setName(description.getName());
                    zones.put(zone.getCode(), zone);

                }
                cache.putInCache(zones, cacheKey);
            }

        } catch (Exception e) {
            LOGGER.error("getZones()", e);
        }
        return zones;


    }

}
