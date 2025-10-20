package com.asrevo.cvhome.catalog.model.product.attribute.api;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductAttributeEntity extends ProductAttributeEntity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String productAttributeWeight;

	private String productAttributePrice;

	private String productAttributeUnformattedPrice;

	private ReadableProductOptionEntity option;

	private ReadableProductOptionValue optionValue;

}
