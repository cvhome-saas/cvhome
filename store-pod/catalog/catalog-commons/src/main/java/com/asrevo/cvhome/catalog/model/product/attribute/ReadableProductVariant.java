package com.asrevo.cvhome.catalog.model.product.attribute;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariant extends Entity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	// option name
	private String name;

	private String code;

	private List<ReadableProductVariantValue> options = new ArrayList<>();

}
