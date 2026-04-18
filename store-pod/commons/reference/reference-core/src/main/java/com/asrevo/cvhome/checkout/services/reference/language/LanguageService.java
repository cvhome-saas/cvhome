package com.asrevo.cvhome.checkout.services.reference.language;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface LanguageService extends SalesManagerEntityService<LanguageCode, Language> {

    Language getByCode(LanguageCode code);

}
