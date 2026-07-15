package com.asrevo.cvhome.checkout.services.reference.country;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.checkout.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CountryService extends SalesManagerEntityService<CountryIsoCode, Country> {

    Country getByCode(CountryIsoCode code);

    List<Country> getCountries(LanguageCode language);

    Map<CountryIsoCode, Country> getCountriesMap(LanguageCode language);

    List<Country> listCountryZones(LanguageCode language);

}
