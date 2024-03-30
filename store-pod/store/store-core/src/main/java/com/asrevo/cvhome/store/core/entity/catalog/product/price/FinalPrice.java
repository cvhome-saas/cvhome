package com.asrevo.cvhome.store.core.entity.catalog.product.price;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Transient entity used to display
 * different price information in the catalogue
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class FinalPrice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<FinalPrice> additionalPrices;
    private BigDecimal discountedPrice = null;//final price if a discount is applied
    private BigDecimal originalPrice = null;//original price
    private BigDecimal finalPrice = null;//final price discount or not
    private boolean discounted = false;
    private int discountPercent = 0;
    private String stringPrice;
    private String stringDiscountedPrice;
    private Date discountEndDate = null;
    private boolean defaultPrice;
    private ProductPrice productPrice;


}
