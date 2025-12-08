package com.asrevo.cvhome.checkout.repositories.reference.language;

import com.asrevo.cvhome.checkout.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, LanguageCode> {

	Language findByCode(LanguageCode code);

}
