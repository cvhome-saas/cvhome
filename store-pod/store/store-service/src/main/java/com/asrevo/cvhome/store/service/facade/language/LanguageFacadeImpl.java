package com.asrevo.cvhome.store.service.facade.language;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageFacadeImpl implements LanguageFacade {

    private final LanguageService languageService;

    public LanguageFacadeImpl(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public List<Language> getLanguages() {
        try {
            List<Language> languages = languageService.getLanguages();
            if (languages.isEmpty()) {
                throw new ResourceNotFoundException("No languages found");
            }
            return languages;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }
}
