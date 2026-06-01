package com.asrevo.cvhome.checkout.model.order;

import com.asrevo.cvhome.store.core.entity.common.Criteria;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderCriteria extends Criteria {

    private String customerName;

    private String customerPhone;

    private String status;

    private Long id;

    private Long customerId;

    private String email;

}
