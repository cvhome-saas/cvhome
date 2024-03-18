package com.asrevo.cvhome.storepod.commons.dto;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;

import java.util.List;

public record CategoriesView(CategoryId id, String name, ImageLink imageLink, Integer sequence, List<CategoriesView> subCategories) {
}
