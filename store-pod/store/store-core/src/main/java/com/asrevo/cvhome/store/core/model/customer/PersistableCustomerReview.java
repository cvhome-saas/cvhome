package com.asrevo.cvhome.store.core.model.customer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class PersistableCustomerReview extends CustomerReviewEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Long reviewedCustomer;

}
