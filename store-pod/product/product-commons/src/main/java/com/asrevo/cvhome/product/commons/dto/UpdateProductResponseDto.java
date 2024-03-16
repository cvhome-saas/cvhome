package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.ImageLink;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.ProductPrice;

public record UpdateProductResponseDto(ProductId id, String name, String description, ProductPrice price,
                                       ImageLink imageLink) {
}
