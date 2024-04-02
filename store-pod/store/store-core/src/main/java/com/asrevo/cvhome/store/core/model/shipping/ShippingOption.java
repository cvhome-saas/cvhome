package com.asrevo.cvhome.store.core.model.shipping;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class ShippingOption implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingOption.class);

    /**
     *
     */
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
                LOGGER.error("Can't convert price text " + this.getOptionPriceText() + " to big decimal");
            }
        }

        return optionPrice;
    }

}
