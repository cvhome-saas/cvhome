package com.asrevo.cvhome.store.commons.dto;

import com.asrevo.cvhome.store.commons.domain.SubProducts;
import com.asrevo.cvhome.storepod.commons.domain.*;

public record CreateProductResponseDto(ProductId id, String name, String description, ProductPrice price,
                                       Boolean published, ImageLink imageLink, CategoryId category, ProductType productType, SubProducts subProducts) {
}
