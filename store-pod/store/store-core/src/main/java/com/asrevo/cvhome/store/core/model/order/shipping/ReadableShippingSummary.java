package com.asrevo.cvhome.store.core.model.order.shipping;


import com.asrevo.cvhome.store.core.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.model.shipping.ShippingOption;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ReadableShippingSummary implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private BigDecimal shipping;
    private BigDecimal handling;
    private String shippingModule;
    private String shippingOption;
    private boolean freeShipping;
    private boolean taxOnShipping;
    private boolean shippingQuote;
    private String shippingText;
    private String handlingText;
    private ReadableDelivery delivery;
    private ShippingOption selectedShippingOption = null;//Default selected option
    private List<ShippingOption> shippingOptions = null;
    private Map<String, String> quoteInformations = new HashMap<>();


}
