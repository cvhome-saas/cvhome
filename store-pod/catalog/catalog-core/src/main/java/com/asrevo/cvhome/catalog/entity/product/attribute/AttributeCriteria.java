package com.asrevo.cvhome.catalog.entity.product.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AttributeCriteria implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String attributeCode;

	private String attributeValue;

}
