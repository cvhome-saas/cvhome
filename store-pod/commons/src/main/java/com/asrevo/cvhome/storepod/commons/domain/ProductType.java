package com.asrevo.cvhome.storepod.commons.domain;

public enum ProductType {
    SINGLE, // single product  or could be added in a VARIANT as ELEMENTS  or in a GROUP AS ELEMENTS

    VARIANT, /*SINGLE_ELEMENTS, */ // product that have different spec and every one has its own
    // price or size  or color

    GROUP, /*SINGLE_ELEMENTS*/ // group of product that might have discount rather over buying
    // single elements
}
