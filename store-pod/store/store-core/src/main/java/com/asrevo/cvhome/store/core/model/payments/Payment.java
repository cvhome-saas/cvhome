package com.asrevo.cvhome.store.core.model.payments;


import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
public class Payment {
	
	private PaymentType paymentType;
	private TransactionType transactionType = TransactionType.AUTHORIZECAPTURE;
	private String moduleName;
	private Currency currency;
	private BigDecimal amount;
	private Map<String,String> paymentMetaData = null;

}
