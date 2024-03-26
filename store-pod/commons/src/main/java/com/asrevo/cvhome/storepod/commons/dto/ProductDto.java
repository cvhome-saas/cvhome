package com.asrevo.cvhome.storepod.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.*;

public record ProductDto(ProductId id, String name, String description, ProductPrice price, ProductType productType,
                         Boolean published,
                         ImagesLink imageLinks, ImageLink imageLink) {
}
