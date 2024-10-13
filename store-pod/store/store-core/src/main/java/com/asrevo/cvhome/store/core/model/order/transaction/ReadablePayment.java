package com.asrevo.cvhome.store.core.model.order.transaction;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadablePayment extends PaymentEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private PaymentType paymentType;
    private TransactionType transactionType;
}
