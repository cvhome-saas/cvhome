package com.asrevo.cvhome.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.content.ContentAudit;

public interface ContentAuditRepository extends JpaRepository<ContentAudit, Long> {
}
