package com.asrevo.cvhome.content.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.PageLayout;

public interface PageLayoutRepository extends JpaRepository<PageLayout, Long> {

    Optional<PageLayout> findByStoreMerchantIdAndPage(String storeMerchantId, String page);

}
