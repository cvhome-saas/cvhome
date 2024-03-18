package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.manager.entity.StoreEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StoreRepository extends CrudRepository<StoreEntity, StoreId> {
    List<StoreEntity> findAllByOwner(IdentityId identityId);
}
