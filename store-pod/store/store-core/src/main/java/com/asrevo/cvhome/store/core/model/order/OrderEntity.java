package com.asrevo.cvhome.store.core.model.order;


import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.order.payment.CreditCard;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.model.order.v0.Order;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class OrderEntity extends Order implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<OrderTotal> totals;
    private List<OrderAttribute> attributes = new ArrayList<OrderAttribute>();

    private PaymentType paymentType;
    private String paymentModule;
    private String shippingModule;
    private List<OrderStatus> previousOrderStatus;
    private OrderStatus orderStatus;
    private CreditCard creditCard;
    private Date datePurchased;
    private String currency;
    private boolean customerAgreed;
    private boolean confirmedAddress;
    private String comments;


}
