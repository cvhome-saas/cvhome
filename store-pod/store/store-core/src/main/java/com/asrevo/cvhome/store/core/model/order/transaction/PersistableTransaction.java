package com.asrevo.cvhome.store.core.model.order.transaction;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This class is used for writing a transaction in the System
 *
 * @author c.samson
 */
@Setter
@Getter
public class PersistableTransaction extends TransactionEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //	@com.salesmanager.shop.validation.Enum(enumClass=PaymentType.class, ignoreCase=true)
    private String paymentType;

    //	@com.salesmanager.shop.validation.Enum(enumClass=TransactionType.class, ignoreCase=true)
    private String transactionType;

}
