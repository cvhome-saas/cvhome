package com.asrevo.cvhome.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.post.ContentPost;

public interface ContentPostRepository extends JpaRepository<ContentPost, Long> {
    Optional<ContentPost> findByIdAndContentStoreMerchantId(Long id, StoreMerchantId store);
}
