package com.asrevo.cvhome.store.core.model.shipping;

import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.system.IntegrationModule;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShippingQuote implements Serializable {

    public static final String NO_SHIPPING_TO_SELECTED_COUNTRY = "NO_SHIPPING_TO_SELECTED_COUNTRY";
    public static final String NO_SHIPPING_MODULE_CONFIGURED = "NO_SHIPPING_MODULE_CONFIGURED";
    public static final String NO_POSTAL_CODE = "NO_POSTAL_CODE";
    public static final String ERROR = "ERROR";

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * shipping module used
     **/
    private String shippingModuleCode;

    private List<ShippingOption> shippingOptions = null;

    /**
     * if an error occurs, this field will be populated from constants defined above
     **/
    private String shippingReturnCode =
            null; // NO_SHIPPING... or NO_SHIPPING_MODULE... or NO_POSTAL_...

    /**
     * indicates if this quote is configured with free shipping
     **/
    private boolean freeShipping;

    /**
     * the threshold amount for being free shipping
     **/
    private BigDecimal freeShippingAmount;

    /**
     * handling fees to be added on top of shipping fees
     **/
    private BigDecimal handlingFees;

    /**
     * apply tax on shipping
     **/
    private boolean applyTaxOnShipping;

    /**
     * final delivery address
     */
    private Delivery deliveryAddress;

    private List<String> warnings = new ArrayList<>();

    private ShippingOption selectedShippingOption = null;

    private IntegrationModule currentShippingModule;

    private String quoteError = null;

    /**
     * additinal shipping information
     **/
    private Map<String, Object> quoteInformations = new HashMap<>();
}
