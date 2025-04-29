package com.asrevo.cvhome.customer.model.customer;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerReview extends CustomerReviewEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ReadableCustomer reviewedCustomer;
}
