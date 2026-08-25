package com.asrevo.cvhome.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.ContentStatusAudit;

public interface ContentStatusAuditRepository extends JpaRepository<ContentStatusAudit, Long> {

    List<ContentStatusAudit> findByContentIdOrderByOccurredAtDesc(Long contentId);

    void deleteByContentId(Long contentId);

}
