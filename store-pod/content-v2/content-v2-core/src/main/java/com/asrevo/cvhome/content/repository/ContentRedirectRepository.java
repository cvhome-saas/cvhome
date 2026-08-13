package com.asrevo.cvhome.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.ContentRedirect;

public interface ContentRedirectRepository extends JpaRepository<ContentRedirect, Long> {
    Optional<ContentRedirect> findByStoreMerchantIdAndLanguageCodeAndOldPath(
            StoreMerchantId store, LanguageCode language, String oldPath);
}
