package com.asrevo.cvhome.catalog.model.product.attribute;

import com.asrevo.cvhome.catalog.model.product.attribute.api.ProductAttributeEntity;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductAttribute extends ProductAttributeEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private BigDecimal productAttributeWeight;

	private BigDecimal productAttributePrice;

	private Long productId;

	private ProductPropertyOption option;

	private PersistableProductOptionValue optionValue;

}
