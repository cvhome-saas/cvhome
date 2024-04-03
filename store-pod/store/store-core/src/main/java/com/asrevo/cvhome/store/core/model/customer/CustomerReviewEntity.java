package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class CustomerReviewEntity extends ShopEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @NotEmpty
    private String description;
    private Long customerId;//review creator
    private String date;

    @NotNull
    @Min(1)
    @Max(5)
    private Double rating;


}
