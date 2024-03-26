package com.asrevo.cvhome.storepod.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;

import java.util.List;

public record DetailedProductDto(ProductId id, ProductDto dto, ProductDetails productDetails,
                                 List<DetailedProductDto> subProducts) {
}
