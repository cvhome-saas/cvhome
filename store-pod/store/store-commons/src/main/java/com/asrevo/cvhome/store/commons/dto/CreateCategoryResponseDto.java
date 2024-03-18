package com.asrevo.cvhome.store.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;

public record CreateCategoryResponseDto(CategoryId id, String name, ImageLink imageLink, CategoryId parent) {
}
