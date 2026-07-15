package com.asrevo.cvhome.checkout.repositories.reference.language;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface LanguageRepository extends JpaRepository<Language, LanguageCode> {

    Language findByCode(LanguageCode code);

}
