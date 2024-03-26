package com.asrevo.cvhome.store.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;

public record CategoryDto(CategoryId id, String name, ImageLink imageLink, Integer sequence, CategoryId parent) {
}
