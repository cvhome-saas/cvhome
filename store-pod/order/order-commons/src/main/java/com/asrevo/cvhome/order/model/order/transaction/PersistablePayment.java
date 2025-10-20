package com.asrevo.cvhome.order.model.order.transaction;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistablePayment extends PaymentEntity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	// @com.salesmanager.shop.validation.Enum(enumClass=PaymentType.class,
	// ignoreCase=true)
	private String paymentType;

	// @com.salesmanager.shop.validation.Enum(enumClass=TransactionType.class,
	// ignoreCase=true)
	private String transactionType;

	private String paymentToken; // any token after doing init

}
