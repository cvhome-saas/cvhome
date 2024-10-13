package com.asrevo.cvhome.store.core.model.shipping;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Package implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String code;
    private double boxWidth = 0;
    private double boxHeight = 0;
    private double boxLength = 0;
    private double boxWeight = 0;
    private double maxWeight = 0;

    /**
     *
     */
    // private int shippingQuantity;
    private int treshold;

    private ShippingPackageType shipPackageType;
    private boolean defaultPackaging;
}
