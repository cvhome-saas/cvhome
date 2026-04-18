package com.asrevo.cvhome.catalog.model.product.product.price;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleProductPrice {

    private BigDecimal productPriceAmount;

    private BigDecimal productPriceSpecialAmount;

    private String code;

    private ProductPriceType productPriceType;

    private boolean defaultPrice;

    private Date productPriceSpecialStartDate;

    private Date productPriceSpecialEndDate;

}
