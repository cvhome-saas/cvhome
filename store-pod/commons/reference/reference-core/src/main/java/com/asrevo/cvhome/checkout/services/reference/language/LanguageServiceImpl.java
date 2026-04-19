package com.asrevo.cvhome.checkout.services.reference.language;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.checkout.repositories.reference.language.LanguageRepository;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

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

}
