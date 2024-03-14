package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.ImageLink;

public record CreateCategoryDto(String name, ImageLink imageLink, Integer sequence) {
}
