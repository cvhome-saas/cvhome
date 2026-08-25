package com.asrevo.cvhome.podregistry.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.podregistry.domain.PodAuditEntity;

@Repository
public interface PodAuditRepository extends CrudRepository<PodAuditEntity, Long> {
}
