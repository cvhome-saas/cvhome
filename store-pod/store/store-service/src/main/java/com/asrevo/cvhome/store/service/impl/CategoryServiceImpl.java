package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;
import com.asrevo.cvhome.store.entity.CategoryEntity;
import com.asrevo.cvhome.store.repository.CategoryRepository;
import com.asrevo.cvhome.store.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoriesView> getStoreCategoriesView(StoreId storeId) {
        List<CategoryEntity> allCategories = categoryRepository.findALlByStoreId(storeId);

        List<CategoriesView> parents = allCategories
                .stream()
                .filter(it -> Objects.isNull(it.getParent()))
                .map(it -> new CategoriesView(it.getId(), it.getName(), it.getImageLink(), it.getSequence(), new ArrayList<>()))
                .sorted(Comparator.comparing(CategoriesView::sequence))
                .toList();

        Map<CategoryId, List<CategoryEntity>> subCategoriesMap = allCategories
                .stream()
                .filter(it -> Objects.nonNull(it.getParent()) && Objects.nonNull(it.getParent().getId()))
                .collect(Collectors.groupingBy(it -> it.getParent().getId()));

        for (CategoriesView parent : parents) {
            List<CategoriesView> subCategories = subCategoriesMap.getOrDefault(parent.id(), List.of())
                    .stream()
                    .map(it -> new CategoriesView(it.getId(), it.getName(), it.getImageLink(), it.getSequence(), new ArrayList<>()))
                    .sorted(Comparator.comparing(CategoriesView::sequence))
                    .toList();
            parent.subCategories().addAll(subCategories);
        }

        return parents;
    }

}
