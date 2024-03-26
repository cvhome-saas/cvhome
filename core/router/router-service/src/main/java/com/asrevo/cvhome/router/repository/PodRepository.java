package com.asrevo.cvhome.router.repository;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.router.entity.PodEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface PodRepository extends ListCrudRepository<PodEntity, PodId>, QueryByExampleExecutor<PodEntity> {

}
