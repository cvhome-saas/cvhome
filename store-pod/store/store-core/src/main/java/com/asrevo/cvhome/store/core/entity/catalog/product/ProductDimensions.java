package com.asrevo.cvhome.store.core.entity.catalog.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductDimensions {

    @Column(name = "LENGTH")
    private BigDecimal length;

    @Column(name = "WIDTH")
    private BigDecimal width;

    @Column(name = "HEIGHT")
    private BigDecimal height;

    @Column(name = "WEIGHT")
    private BigDecimal weight;
}
