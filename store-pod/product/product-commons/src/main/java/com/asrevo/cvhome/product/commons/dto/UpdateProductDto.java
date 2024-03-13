package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductPrice;

public record UpdateProductDto(String name, String description, ProductPrice price, Boolean published) {
}
