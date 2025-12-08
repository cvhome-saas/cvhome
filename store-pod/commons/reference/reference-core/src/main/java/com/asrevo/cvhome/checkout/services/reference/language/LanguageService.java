package com.asrevo.cvhome.checkout.services.reference.language;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.Locale;
import java.util.Map;

public interface LanguageService extends SalesManagerEntityService<LanguageCode, Language> {

	Language getByCode(LanguageCode code);

	Map<LanguageCode, Language> getLanguagesMap() throws ServiceException;

	Locale toLocale(LanguageCode language, CountryIsoCode country);

}
