package com.asrevo.cvhome.store.core.model.customer;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCustomerReview extends CustomerReviewEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Long reviewedCustomer;
}
