package com.asrevo.cvhome.store.commons.dto;

import com.asrevo.cvhome.store.commons.domain.SubProducts;
import com.asrevo.cvhome.storepod.commons.domain.*;

public record UpdateProductDto(String name, String description, ProductPrice price,
                               ImageLink imageLink, ProductAmount amount, ProductType productType,
                               SubProducts subProducts, CategoryId categoryId) {
}