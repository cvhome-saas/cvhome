package com.asrevo.cvhome.store.core.model.payments;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.system.IntegrationConfiguration;
import com.asrevo.cvhome.store.core.entity.system.IntegrationModule;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Object to be used in store front with meta data and configuration
 * informations required to display to the end user
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class PaymentMethod implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String paymentMethodCode;
    private PaymentType paymentType;
    private boolean defaultSelected;
    private IntegrationModule module;
    private IntegrationConfiguration informations;
}
