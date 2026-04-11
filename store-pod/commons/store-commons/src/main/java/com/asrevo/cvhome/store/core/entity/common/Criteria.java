package com.asrevo.cvhome.store.core.entity.common;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

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
