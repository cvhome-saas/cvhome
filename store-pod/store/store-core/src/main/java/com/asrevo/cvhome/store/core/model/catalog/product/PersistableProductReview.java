package com.asrevo.cvhome.store.core.model.catalog.product;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductReview extends ProductReviewEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @NotNull private Long customerId;
}
