package com.asrevo.cvhome.store.core.model.shipping;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Slf4j
public class ShippingOption implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private BigDecimal optionPrice;
    private Long shippingQuoteOptionId;


    private String optionName = null;
    private String optionCode = null;
    private String optionDeliveryDate = null;
    private String optionShippingDate = null;
    private String optionPriceText = null;
    private String optionId = null;
    private String description = null;
    private String shippingModuleCode = null;
    private String note = null;

    private String estimatedNumberOfDays;


    public BigDecimal getOptionPrice() {

        if (optionPrice == null && !StringUtils.isBlank(this.getOptionPriceText())) {//if price text only is available, try to parse it
            try {
                this.optionPrice = new BigDecimal(this.getOptionPriceText());
            } catch (Exception e) {
                log.error("Can't convert price text {} to big decimal", this.getOptionPriceText());
            }
        }

        return optionPrice;
    }

}
