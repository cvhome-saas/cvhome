package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ProductType;

public record FindAllProductDto(CategoryId category, Boolean published, String name, ProductType productType) {

}
