package com.asrevo.cvhome.store.core.model.order.transaction;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Readable version of Transaction entity object
 *
 * @author c.samson
 */
@Setter
@Getter
public class TransactionEntity extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String details;
    private String transactionDate;
    private String amount;


}
