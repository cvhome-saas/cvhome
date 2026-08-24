package com.asrevo.cvhome.inventory.model.price;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

import static com.asrevo.cvhome.store.core.constants.Constants.DEFAULT_PRICE_CODE;

/**
 * Common fields of a product price as it crosses the API.
 */
@Setter
@Getter
public class ProductPriceEntity extends Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private boolean discounted = false;

    private LocalDate discountStartDate;

    private LocalDate discountEndDate;

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
