package com.asrevo.cvhome.store.core.repositories.catalog.category;

import com.asrevo.cvhome.store.core.entity.catalog.category.CategoryDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CategoryDescriptionRepository extends JpaRepository<CategoryDescription, Long> {


    @Query("select c from CategoryDescription c where c.category.id = ?1")
    List<CategoryDescription> listByCategoryId(Long categoryId);


}
