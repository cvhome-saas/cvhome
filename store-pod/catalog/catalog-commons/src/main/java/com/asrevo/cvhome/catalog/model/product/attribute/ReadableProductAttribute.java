package com.asrevo.cvhome.catalog.model.product.attribute;

import com.asrevo.cvhome.catalog.model.product.attribute.api.ProductAttributeEntity;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductAttribute extends ProductAttributeEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String name;

	private String lang;

	private String code;

	private String type;

	private List<ReadableProductAttributeValue> attributeValues = new ArrayList<>();

}
