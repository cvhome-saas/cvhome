package com.asrevo.cvhome.checkout.model.order;

import com.asrevo.cvhome.checkout.model.order.total.OrderTotal;
import com.asrevo.cvhome.checkout.model.order.v0.Order;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.order.payment.CreditCard;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderEntity extends Order implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private List<OrderTotal> totals;

	private List<OrderAttribute> attributes = new ArrayList<>();

	private PaymentType paymentType;

	private String paymentModule;

	private String shippingModule;

	private List<OrderStatus> previousOrderStatus;

	private OrderStatus orderStatus;

	private CreditCard creditCard;

	private Date datePurchased;

	@JsonSerialize(using = CurrencyCodeSerializer.class)
	@JsonDeserialize(using = CurrencyCodeDeSerializer.class)
	private CurrencyCode currency; // code

	private boolean customerAgreed;

	private boolean confirmedAddress;

	private String comments;

}
