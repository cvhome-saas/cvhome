package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * An order line as snapshotted at placement. {@code price} and {@code subTotal} are formatted strings because that is
 * what both frontends render; the numbers live on the entity.
 */
@Getter
@Setter
public class ReadableOrderProduct extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;

    private String productName;

    private int orderedQuantity;

    private String price;

    private String subTotal;

    private String image;

    private List<ReadableOrderProductAttribute> attributes;
}
