package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductPrice;

public record CreateProductDto(String name, String description, ProductPrice price, ImageLink imageLink, CategoryId categoryId) {
}
