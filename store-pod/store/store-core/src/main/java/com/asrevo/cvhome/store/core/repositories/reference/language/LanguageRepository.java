package com.asrevo.cvhome.store.core.repositories.reference.language;

import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language, Integer> {

    Language findByCode(String code) throws ServiceException;
}
