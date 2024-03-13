package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ProductId;

public record DeleteProductResponseDto(ProductId id, Boolean deleted) {
}
