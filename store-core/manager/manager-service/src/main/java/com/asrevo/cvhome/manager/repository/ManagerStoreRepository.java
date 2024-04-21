package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface ManagerStoreRepository extends CrudRepository<ManagerStoreEntity, ManagerStoreId>, QueryByExampleExecutor<ManagerStoreEntity> {
    List<ManagerStoreEntity> findAllByOwner(IdentityId identityId);

    Boolean existsByName(String name);
}
