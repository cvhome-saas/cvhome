package com.asrevo.cvhome.store.core.services.reference.language;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.reference.language.LanguageRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * https://samerabdelkafi.wordpress.com/2014/05/29/spring-data-jpa/
 *
 * @author c.samson
 */

@Service("languageService")
@Slf4j
public class LanguageServiceImpl extends SalesManagerEntityServiceImpl<Integer, Language>
        implements LanguageService {


    private final CacheUtils cache;

    private final LanguageRepository languageRepository;

    @Autowired
    public LanguageServiceImpl(LanguageRepository languageRepository, CacheUtils cache) {
        super(languageRepository);
        this.languageRepository = languageRepository;
        this.cache = cache;
    }


    @Override
    @Cacheable("languageByCode")
    public Language getByCode(String code) throws ServiceException {
        return languageRepository.findByCode(code);
    }

    @Override
    public Locale toLocale(Language language, MerchantStore store) {

        if (store != null) {

            String countryCode = store.getCountry().getIsoCode();

            return new Locale(language.getCode(), countryCode);

        } else {

            return new Locale(language.getCode());
        }
    }

    @Override
    public Language toLanguage(Locale locale) {
        Language language = null;
        try {
            language = getLanguagesMap().get(locale.getLanguage());
        } catch (Exception e) {
            log.error("Cannot convert locale {} to language", locale.getLanguage());
        }
        if (language == null) {
            language = new Language(Constants.DEFAULT_LANGUAGE);
        }
        return language;

    }

    @Override
    public Map<String, Language> getLanguagesMap() throws ServiceException {

        List<Language> langs = this.getLanguages();
        Map<String, Language> returnMap = new LinkedHashMap<String, Language>();

        for (Language lang : langs) {
            returnMap.put(lang.getCode(), lang);
        }
        return returnMap;

    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Language> getLanguages() throws ServiceException {


        List<Language> langs = null;
        try {

            langs = (List<Language>) cache.getFromCache("LANGUAGES");
            if (langs == null) {
                langs = this.list();


                cache.putInCache(langs, "LANGUAGES");
            }

        } catch (Exception e) {
            log.error("getCountries()", e);
            throw new ServiceException(e);
        }

        return langs;

    }

    @Override
    public Language defaultLanguage() {
        return toLanguage(Locale.ENGLISH);
    }

}
