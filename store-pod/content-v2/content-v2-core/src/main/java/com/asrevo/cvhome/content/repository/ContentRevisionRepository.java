package com.asrevo.cvhome.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.content.ContentRevision;

public interface ContentRevisionRepository extends JpaRepository<ContentRevision, Long> {
    List<ContentRevision> findAllByContentIdOrderByVersionDesc(Long contentId);
}
