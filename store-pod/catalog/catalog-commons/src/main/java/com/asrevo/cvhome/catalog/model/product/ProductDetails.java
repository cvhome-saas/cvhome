package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;

public record ProductDetails(
        ReadableMinimalProduct product,
        FinalPrice price,
        ReadableProductAvailability availability) {}
