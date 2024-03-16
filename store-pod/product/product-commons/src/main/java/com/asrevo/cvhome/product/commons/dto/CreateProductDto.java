package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.*;

public record CreateProductDto(String name, String description, ProductPrice price, ImageLink imageLink,
                               ProductAmount amount, ProductType productType,ImagesLink imageLinks,CategoryId category) {
}
