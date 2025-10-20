package com.asrevo.cvhome.order.services.reference.country;

import com.asrevo.cvhome.order.entity.reference.country.Country;
import com.asrevo.cvhome.order.repositories.reference.country.CountryRepository;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("countryService")
@Slf4j
public class CountryServiceImpl extends SalesManagerEntityServiceImpl<CountryIsoCode, Country>
		implements CountryService {

	private final CountryRepository countryRepository;

	@Autowired
	public CountryServiceImpl(CountryRepository countryRepository) {
		super(countryRepository);
		this.countryRepository = countryRepository;
	}

	@Cacheable("countrByCode")
	public Country getByCode(CountryIsoCode code) {
		return countryRepository.findByIsoCode(code);
	}

	@Override
	@Cacheable("countriesMap")
	public Map<CountryIsoCode, Country> getCountriesMap(LanguageCode language) {

		List<Country> countries = getCountries(language);

		Map<CountryIsoCode, Country> returnMap = new LinkedHashMap<>();

		for (Country country : countries) {
			returnMap.put(country.getIsoCode(), country);
		}

		return returnMap;
	}

	@Override
	public List<Country> listCountryZones(LanguageCode language) {
		return countryRepository.listCountryZonesByLanguage(language);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Country> getCountries(LanguageCode language) {
		return countryRepository.listByLanguage(language);
	}

}
