package com.asrevo.cvhome.store.core.services.reference.language;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface LanguageService extends SalesManagerEntityService<Integer, Language> {

    Language getByCode(String code) throws ServiceException;

    Map<String, Language> getLanguagesMap() throws ServiceException;

    List<Language> getLanguages() throws ServiceException;

    Locale toLocale(Language language, MerchantStore store);

    Language toLanguage(Locale locale);

    Language defaultLanguage();
}
