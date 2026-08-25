package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.content.entity.Menu;
import com.asrevo.cvhome.content.model.MenuHandle;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("select m from Menu m left join fetch m.items where m.storeMerchantId = :store and m.handle = :handle")
    Optional<Menu> findByStoreAndHandle(@Param("store") String store, @Param("handle") MenuHandle handle);

    @Query("select distinct m from Menu m left join fetch m.items where m.storeMerchantId = :store")
    List<Menu> findByStore(@Param("store") String store);

}
