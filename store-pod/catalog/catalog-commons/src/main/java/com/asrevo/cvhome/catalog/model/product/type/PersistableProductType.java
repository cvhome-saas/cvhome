package com.asrevo.cvhome.catalog.model.product.type;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductType extends ProductTypeEntity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<ProductTypeDescription> descriptions;

}
