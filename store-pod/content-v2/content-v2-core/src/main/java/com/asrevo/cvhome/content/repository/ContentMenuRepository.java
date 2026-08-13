package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.menu.ContentMenu;

public interface ContentMenuRepository extends JpaRepository<ContentMenu, Long> {
    Optional<ContentMenu> findByIdAndStoreMerchantId(Long id, StoreMerchantId store);

    Optional<ContentMenu> findByStoreMerchantIdAndHandle(StoreMerchantId store, String handle);

    List<ContentMenu> findAllByStoreMerchantIdOrderByHandleAsc(StoreMerchantId store);
}
