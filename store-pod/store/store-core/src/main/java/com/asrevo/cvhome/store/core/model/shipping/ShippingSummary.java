package com.asrevo.cvhome.store.core.model.shipping;

import com.asrevo.cvhome.store.core.entity.common.Delivery;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * Contains shipping fees according to user selections
 *
 * @author casams1
 */
@Setter
@Getter
public class ShippingSummary implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private BigDecimal shipping;
    private BigDecimal handling;
    private String shippingModule;
    private String shippingOption;
    private String shippingOptionCode;
    private boolean freeShipping;
    private boolean taxOnShipping;
    private boolean shippingQuote;

    private Delivery deliveryAddress;


}
