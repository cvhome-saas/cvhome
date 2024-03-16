package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.*;

import java.util.List;

public record ProductDto(ProductId id, String name, String description, ProductPrice price, ProductType productType, Boolean published,
                         ImagesLink imageLinks,ImageLink imageLink) {
}
