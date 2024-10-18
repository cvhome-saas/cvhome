package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerReviewEntity extends ShopEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @NotEmpty private String description;
    private Long customerId; // review creator
    private String date;

    @NotNull @Min(1)
    @Max(5)
    private Double rating;
}
