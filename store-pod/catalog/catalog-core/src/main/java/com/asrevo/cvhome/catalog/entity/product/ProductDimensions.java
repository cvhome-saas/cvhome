package com.asrevo.cvhome.catalog.entity.product;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ProductDimensions implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "LENGTH")
    private BigDecimal length;

    @Column(name = "WIDTH")
    private BigDecimal width;

    @Column(name = "HEIGHT")
    private BigDecimal height;

    @Column(name = "WEIGHT")
    private BigDecimal weight;

}
