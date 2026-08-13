package com.asrevo.cvhome.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.content.ContentStatusAudit;

public interface ContentStatusAuditRepository extends JpaRepository<ContentStatusAudit, Long> {
}
