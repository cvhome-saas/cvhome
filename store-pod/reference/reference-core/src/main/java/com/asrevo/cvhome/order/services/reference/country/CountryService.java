package com.asrevo.cvhome.order.services.reference.country;

import com.asrevo.cvhome.order.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Map;

public interface CountryService extends SalesManagerEntityService<CountryIsoCode, Country> {

	Country getByCode(CountryIsoCode code);

	List<Country> getCountries(LanguageCode language);

	Map<CountryIsoCode, Country> getCountriesMap(LanguageCode language);

	List<Country> listCountryZones(LanguageCode language);

}
