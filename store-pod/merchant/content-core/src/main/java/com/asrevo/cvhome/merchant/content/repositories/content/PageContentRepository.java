package com.asrevo.cvhome.merchant.content.repositories.content;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface PageContentRepository
        extends PagingAndSortingRepository<Content, Long>, JpaSpecificationExecutor<Content> {

    default Page<Content> findByContentType(ContentType contentTypes, StoreMerchantId storeMerchantId,
                                            LanguageCode languageCode, Pageable pageable) {
        Specification<Content> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("storeMerchantId"), storeMerchantId));
            predicates.add(cb.equal(root.get("contentType"), contentTypes));
            if (LanguageCode.isLanguage(languageCode)) {
                predicates.add(cb.equal(root.get("descriptions").get("languageCode"), languageCode));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return findAll(specification, pageable);
    }

}
