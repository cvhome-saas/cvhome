package com.asrevo.cvhome.storepod.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ProductType;

public record FindAllProductDto(CategoryId category, Boolean published, String name, ProductType productType) {

}
