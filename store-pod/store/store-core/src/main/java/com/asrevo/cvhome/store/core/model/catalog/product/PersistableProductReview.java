package com.asrevo.cvhome.store.core.model.catalog.product;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class PersistableProductReview extends ProductReviewEntity implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @NotNull
    private Long customerId;


}
