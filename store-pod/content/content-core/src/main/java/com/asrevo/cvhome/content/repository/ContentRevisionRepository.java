package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.ContentRevision;

public interface ContentRevisionRepository extends JpaRepository<ContentRevision, Long> {

    List<ContentRevision> findByContentIdOrderByVersionDesc(Long contentId);

    Optional<ContentRevision> findByContentIdAndVersion(Long contentId, Integer version);

    void deleteByContentId(Long contentId);

}
