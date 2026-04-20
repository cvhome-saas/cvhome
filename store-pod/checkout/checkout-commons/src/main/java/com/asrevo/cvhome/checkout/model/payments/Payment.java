package com.asrevo.cvhome.checkout.model.payments;

import java.math.BigDecimal;
import java.util.Map;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Payment {

    private PaymentType paymentType;

    private TransactionType transactionType = TransactionType.AUTHORIZECAPTURE;

    private String moduleName;

    private CurrencyCode currency;

    private BigDecimal amount;

    private Map<String, String> paymentMetaData = null;

}
