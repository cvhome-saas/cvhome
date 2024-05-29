package com.asrevo.cvhome.store.core.model.catalog.product.product.definition;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductSpecification;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

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
    private String sku; //to match v1 api
    private ProductSpecification productSpecifications;
    private int sortOrder;


}
