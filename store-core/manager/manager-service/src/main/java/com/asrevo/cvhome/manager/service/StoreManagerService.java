package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import java.util.Map;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface StoreManagerService {
    Mono<Void> createStore(IdentityId identityId, Map<Object, Object> request);

    Mono<PageImpl<Object>> findAll(
            UserOrgStoreIdentity identity,
            ListManagerStoreQuery listManagerStoreQuery,
            Pageable pageable);

    Mono<Object> getStore(ManagerStoreId managerStoreId);
}
