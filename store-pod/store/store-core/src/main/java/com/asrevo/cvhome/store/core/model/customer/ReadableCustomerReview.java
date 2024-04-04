package com.asrevo.cvhome.store.core.model.customer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableCustomerReview extends CustomerReviewEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ReadableCustomer reviewedCustomer;


}
