package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ManagerStoreRepository extends CrudRepository<ManagerStoreEntity, ManagerStoreId> {
    List<ManagerStoreEntity> findAllByOwner(IdentityId identityId);
}
