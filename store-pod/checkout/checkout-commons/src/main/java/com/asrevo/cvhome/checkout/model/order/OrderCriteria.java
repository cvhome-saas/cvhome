package com.asrevo.cvhome.checkout.model.order;

import com.asrevo.cvhome.store.core.entity.common.Criteria;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderCriteria extends Criteria {

    private String customerName = null;

    private String customerPhone = null;

    private String status = null;

    private Long id = null;

    private String paymentMethod;

    private Long customerId;

    private String email;

}
