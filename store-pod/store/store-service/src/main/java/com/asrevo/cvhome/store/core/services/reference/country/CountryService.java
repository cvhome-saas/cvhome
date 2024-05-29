package com.asrevo.cvhome.store.core.services.reference.country;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.country.CountryDescription;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;
import java.util.Map;

public interface CountryService extends SalesManagerEntityService<Integer, Country> {

    Country getByCode(String code) throws ServiceException;

    void addCountryDescription(Country country, CountryDescription description) throws ServiceException;

    List<Country> getCountries(Language language) throws ServiceException;

    Map<String, Country> getCountriesMap(Language language)
            throws ServiceException;

    List<Country> getCountries(List<String> isoCodes, Language language)
            throws ServiceException;


    /**
     * List country - zone objects by language
     *
     */
    List<Country> listCountryZones(Language language) throws ServiceException;
}
