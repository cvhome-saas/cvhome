package com.asrevo.cvhome.catalog.model.product.type;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableProductTypeList extends ReadableList<ReadableProductType> {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	List<ReadableProductType> content = new ArrayList<>();

}
