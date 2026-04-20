package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.catalog.model.product.product.price.FinalPriceCalc;

public record ProductDetails(ReadableMinimalProduct product, FinalPriceCalc price,
                             ReadableProductAvailability availability) {
}
