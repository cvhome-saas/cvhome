package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;

import java.util.List;

public record ProductDto(ProductId id, String name, String description, ProductPrice price, List<String> images) {
}
