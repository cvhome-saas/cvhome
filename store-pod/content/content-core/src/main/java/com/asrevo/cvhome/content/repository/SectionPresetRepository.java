package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.SectionPreset;

public interface SectionPresetRepository extends JpaRepository<SectionPreset, Long> {

    List<SectionPreset> findByStoreMerchantIdOrderByDateCreatedDesc(String storeMerchantId);

    Optional<SectionPreset> findByIdAndStoreMerchantId(Long id, String storeMerchantId);

}
