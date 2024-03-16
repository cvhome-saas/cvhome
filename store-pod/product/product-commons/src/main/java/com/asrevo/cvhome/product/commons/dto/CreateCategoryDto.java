package com.asrevo.cvhome.product.commons.dto;


import com.asrevo.cvhome.storepod.commons.domain.ImageLink;

public record CreateCategoryDto(String name, ImageLink imageLink, Integer sequence) {
}
