package com.asrevo.cvhome.catalog.model.product.product.definition;

import com.asrevo.cvhome.catalog.model.product.product.ProductSpecification;
import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * Applies to product version 2 management
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ProductDefinition extends Entity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private boolean visible = true;

	private boolean shipeable = true;

	private boolean virtual = false;

	private boolean canBePurchased = true;

	private String dateAvailable;

	private String identifier;

	private String sku; // to match v1 api

	private ProductSpecification productSpecifications;

	private int sortOrder;

}
