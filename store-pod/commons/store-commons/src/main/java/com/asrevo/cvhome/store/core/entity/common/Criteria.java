package com.asrevo.cvhome.store.core.entity.common;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Criteria {

    private Pageable pageable;

    private String code;

    private String name;

    private LanguageCode language;

    private CriteriaOrderBy orderBy = CriteriaOrderBy.DESC;

    private String search;

}
