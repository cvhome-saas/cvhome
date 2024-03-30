package com.asrevo.cvhome.store.core.services.reference.zone;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;

import java.util.List;
import java.util.Map;

public interface ZoneService extends SalesManagerEntityService<Long, Zone> {

    Zone getByCode(String code);

    void addDescription(Zone zone, ZoneDescription description) throws ServiceException;

    List<Zone> getZones(Country country, Language language)
            throws ServiceException;

    Map<String, Zone> getZones(Language language) throws ServiceException;

    List<Zone> getZones(String countryCode, Language language) throws ServiceException;


}
