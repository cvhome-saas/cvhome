package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductAttributeValue extends ProductOptionValue {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String name;

	private String lang;

	private String description;

}
