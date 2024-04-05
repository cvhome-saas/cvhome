package com.asrevo.cvhome.store.core.model.order.transaction;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class PersistablePayment extends PaymentEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    //	@com.salesmanager.shop.validation.Enum(enumClass=PaymentType.class, ignoreCase=true)
    private String paymentType;

    //	@com.salesmanager.shop.validation.Enum(enumClass=TransactionType.class, ignoreCase=true)
    private String transactionType;

    private String paymentToken;//any token after doing init


}
