package com.asrevo.cvhome.store.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.domain.ImageLink;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("category")
public class CategoryEntity extends BaseEntity<CategoryEntity, CategoryId> {
    private StoreId storeId;
    private String name;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private ImageLink imageLink;
    private Integer level = 0;
    private Integer sequence = 0;
    @Column("parent_id")
    private AggregateReference<CategoryEntity, CategoryId> parent;

    public static CategoryEntity createCategory(StoreId storeId, CreateCategoryDto createCategoryDto) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setNew();
        categoryEntity.setStoreId(storeId);
        categoryEntity.setName(createCategoryDto.name());
        categoryEntity.setImageLink(createCategoryDto.imageLink());
        categoryEntity.setLevel(0);
        categoryEntity.setSequence(createCategoryDto.sequence());
        return categoryEntity;
    }

    public static CategoryEntity createCategory(StoreId storeId, CategoryEntity parent, CreateCategoryDto createCategoryDto) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setNew();
        categoryEntity.setStoreId(storeId);
        categoryEntity.setParent(AggregateReference.to(parent.getId()));
        categoryEntity.setName(createCategoryDto.name());
        categoryEntity.setImageLink(createCategoryDto.imageLink());
        categoryEntity.setLevel(parent.getLevel() + 1);
        categoryEntity.setSequence(createCategoryDto.sequence());
        return categoryEntity;
    }

    @Override
    protected CategoryId generateId() {
        return CategoryId.newId();
    }
}
