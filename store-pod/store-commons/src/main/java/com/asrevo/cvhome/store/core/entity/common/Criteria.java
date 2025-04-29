package com.asrevo.cvhome.store.core.entity.common;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Criteria {

    // legacy pagination
    private int startIndex = 0;
    private int maxCount = 0;
    // new pagination
    private int startPage = 0;
    private int pageSize = 10;
    private boolean legacyPagination = true;
    private String code;
    private String name;
    private LanguageCode language;
    private String user;
    private String storeCode;
    private List<Integer> storeIds;

    private CriteriaOrderBy orderBy = CriteriaOrderBy.DESC;
    private String criteriaOrderByField;
    private String search;
}
