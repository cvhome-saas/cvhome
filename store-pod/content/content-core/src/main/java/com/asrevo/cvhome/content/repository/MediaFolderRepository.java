package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.MediaFolder;

public interface MediaFolderRepository extends JpaRepository<MediaFolder, Long> {

    List<MediaFolder> findByStoreMerchantIdOrderByPositionAscIdAsc(String store);

    Optional<MediaFolder> findByIdAndStoreMerchantId(Long id, String store);

    Optional<MediaFolder> findByStoreMerchantIdAndKey(String store, String key);

}
