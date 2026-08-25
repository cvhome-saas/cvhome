package com.asrevo.cvhome.podregistry.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.podregistry.domain.PodHealthCheckEntity;

@Repository
public interface PodHealthCheckRepository extends CrudRepository<PodHealthCheckEntity, Long> {
}
