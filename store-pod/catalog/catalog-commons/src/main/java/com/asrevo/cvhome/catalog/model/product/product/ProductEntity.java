package com.asrevo.cvhome.catalog.model.product.product;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.catalog.model.product.Product;

import lombok.Getter;
import lombok.Setter;

/**
 * A product entity is used by services API to populate or retrieve a Product entity
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class ProductEntity extends Product implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;

    private boolean preOrder = false;

    private boolean productVirtual = false;

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
