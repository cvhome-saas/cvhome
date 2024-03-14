package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;

public record CategoryDto(CategoryId id, String name, ImageLink imageLink, Integer sequence, CategoryId parent) {
}
