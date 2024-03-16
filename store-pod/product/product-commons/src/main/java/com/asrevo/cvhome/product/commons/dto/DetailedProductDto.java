package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductDetails;
import com.asrevo.cvhome.product.commons.domain.ProductId;

import java.util.List;

public record DetailedProductDto(ProductId id, ProductDto dto, ProductDetails productDetails,
                                 List<DetailedProductDto> subProducts) {
}
