package com.asrevo.cvhome.store.core.model.customer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerReview extends CustomerReviewEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ReadableCustomer reviewedCustomer;


}
