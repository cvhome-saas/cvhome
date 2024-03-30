package com.asrevo.cvhome.store.core.model.catalog.product;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A product entity is used by services API
 * to populate or retrieve a Product price entity
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class ProductPriceEntity extends ProductPrice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean discounted = false;
    private String discountStartDate;
    private String discountEndDate;
    private boolean defaultPrice = true;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    public String getCode() {
        if (StringUtils.isBlank(this.code)) {
            code = DEFAULT_PRICE_CODE;
        }
        return code;
    }


}
