package com.asrevo.cvhome.router.repository;

import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.entity.PodEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface PodRepository extends ListCrudRepository<PodEntity, PodId> {
}
