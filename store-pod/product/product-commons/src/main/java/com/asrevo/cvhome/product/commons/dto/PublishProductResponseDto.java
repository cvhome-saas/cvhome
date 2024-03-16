package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.ProductId;

public record PublishProductResponseDto(ProductId id, Boolean published) {
}
