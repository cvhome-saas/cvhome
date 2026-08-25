package com.asrevo.cvhome.tenancy.manager.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.tenancy.manager.entity.TenancyAuditEntity;

@Repository
public interface TenancyAuditRepository extends CrudRepository<TenancyAuditEntity, Long> {
}
