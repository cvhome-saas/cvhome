package com.asrevo.cvhome.catalog.model.category;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableCategoryList extends ReadableList<ReadableCategory> {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<ReadableCategory> content = new ArrayList<>();

}
