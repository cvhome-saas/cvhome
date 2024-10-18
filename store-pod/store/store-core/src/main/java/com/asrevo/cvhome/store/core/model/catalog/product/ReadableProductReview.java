package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductReview extends ProductReviewEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ReadableCustomer customer;
}
