package com.asrevo.cvhome.catalog.repositories.category;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;

public interface PageableCategoryRepository
        extends PagingAndSortingRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    default Page<Category> listByStore(StoreMerchantId storeMerchantId, LanguageCode languageCode, String name,
                                       Pageable pageable) {
        Specification<Category> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("storeMerchantId"), storeMerchantId));
            if (LanguageCode.isLanguage(languageCode)) {
                predicates.add(cb.equal(root.get("descriptions").get("languageCode"), languageCode));
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), Constants.PERCENT_SYMBOL + name + Constants.PERCENT_SYMBOL));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return findAll(spec, pageable);
    }

}
