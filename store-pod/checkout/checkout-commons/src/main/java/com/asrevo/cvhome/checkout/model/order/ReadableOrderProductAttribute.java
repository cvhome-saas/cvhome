package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One option/value label pair snapshotted at placement ("Color" / "Red"). {@code attributePrice} is kept for the
 * console's model and never filled.
 */
@Getter
@Setter
public class ReadableOrderProductAttribute extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String attributeName;

    private String attributeValue;

    private String attributePrice;
}
