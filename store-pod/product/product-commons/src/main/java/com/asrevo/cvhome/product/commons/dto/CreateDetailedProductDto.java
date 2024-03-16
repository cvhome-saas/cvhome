package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;

import java.util.List;

public record CreateDetailedProductDto(CreateProductDto dto, ProductDetails productDetails,
                                       List<DetailedProductDto> subProducts) {
    public CreateDetailedProductDto(CreateProductDto dto) {
        this(dto, null, null);
    }

    public CreateDetailedProductDto(CreateProductDto dto, ProductDetails productDetails) {
        this(dto, productDetails, null);
    }
}
