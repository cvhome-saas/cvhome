package com.asrevo.cvhome.product.commons.dto;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;

import java.util.List;

public record CategoriesView(CategoryId id, String name, ImageLink imageLink,Integer sequence, List<CategoriesView> subCategories) {
}
