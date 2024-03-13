package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductImageId;

public record AddProductImageResponseDto(ProductImageId id, ImageLink imageLink, ProductId productId) {
}
