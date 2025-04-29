package com.asrevo.cvhome.catalog.model.product.product;

import com.asrevo.cvhome.catalog.model.product.Product;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * A product entity is used by services API to populate or retrieve a Product
 * entity
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class ProductEntity extends Product implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     * End RENTAL fields
     */
    private BigDecimal price;

    private int quantity = 0;
    private String sku;
    private boolean preOrder = false;
    private boolean productVirtual = false;
    private int quantityOrderMaximum = -1; // default unlimited
    private int quantityOrderMinimum = 1; // default 1
    private boolean productIsFree;

    private ProductSpecification productSpecifications;
    private Double rating = 0D;
    private int ratingCount;
    private int sortOrder;
    private String refSku;

    /**
     * RENTAL additional fields
     */
    private int rentalDuration;

    private int rentalPeriod;
}
