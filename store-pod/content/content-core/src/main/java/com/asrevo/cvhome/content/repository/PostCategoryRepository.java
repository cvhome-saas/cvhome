package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.PostCategory;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    List<PostCategory> findByStoreMerchantIdOrderByPositionAscIdAsc(String store);

    Optional<PostCategory> findByIdAndStoreMerchantId(Long id, String store);

    Optional<PostCategory> findByStoreMerchantIdAndSlug(String store, String slug);

}
