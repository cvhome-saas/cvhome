package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;

public record CreateProductResponseDto(ProductId id, String name, String description, ProductPrice price,
                                       Boolean published, ImageLink imageLink) {
}
