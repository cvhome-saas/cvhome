package com.asrevo.cvhome.checkout.services.reference.language;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.checkout.repositories.reference.language.LanguageRepository;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <a href="https://samerabdelkafi.wordpress.com/2014/05/29/spring-data-jpa/">...</a>
 *
 * @author c.samson
 */
@Service("languageService")
@Slf4j
public class LanguageServiceImpl extends SalesManagerEntityServiceImpl<LanguageCode, Language>
		implements LanguageService {

	private final LanguageRepository languageRepository;

	@Autowired
	public LanguageServiceImpl(LanguageRepository languageRepository) {
		super(languageRepository);
		this.languageRepository = languageRepository;
	}

	@Override
	@Cacheable("languageByCode")
	public Language getByCode(LanguageCode code) {
		return languageRepository.findByCode(code);
	}

	@Override
	public Locale toLocale(LanguageCode language, CountryIsoCode country) {
		return Locale.of(language.code(), country.isoCode());
	}

	@Override
	public Map<LanguageCode, Language> getLanguagesMap() throws ServiceException {

		List<Language> langs = this.getLanguages();
		Map<LanguageCode, Language> returnMap = new LinkedHashMap<>();

		for (Language lang : langs) {
			returnMap.put(lang.getCode(), lang);
		}
		return returnMap;
	}

	@SuppressWarnings("unchecked")
	private List<Language> getLanguages() {
		return this.list();
	}

}
