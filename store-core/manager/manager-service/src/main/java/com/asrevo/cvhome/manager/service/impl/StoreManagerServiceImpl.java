package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreManagerServiceImpl implements StoreManagerService {
    private final SaasProperties saasProperties;
    private final RouterService routerService;
    private final InternalStoreService internalStoreService;
    private final ManagerStoreMappers managerStoreMappers;
    private final StorePodClient storePodClient;

    public StoreManagerServiceImpl(SaasProperties saasProperties, RouterService routerService, InternalStoreService internalStoreService, ManagerStoreMappers managerStoreMappers, StorePodClient storePodClient) {
        this.saasProperties = saasProperties;
        this.routerService = routerService;
        this.internalStoreService = internalStoreService;
        this.managerStoreMappers = managerStoreMappers;
        this.storePodClient = storePodClient;
    }

    @Override
    public Mono<Void> createStore(IdentityId identityId, Map<Object, Object> request) {
        CreateManagerStoreRequest storeRequest = managerStoreMappers.toCreateStoreRequest(request);
        ManagerStoreDto store = internalStoreService.createStore(storeRequest, identityId);
        Domain suggestedSubDomain = new Domain(storeRequest.name() + "." + saasProperties.getDefaultDomain());
        routerService.create(suggestedSubDomain, store.id());
        internalStoreService.syncInRouter(store.id());
        Map<Object, Object> newRequest = managerStoreMappers.toExternalCreateRequest(request, store.id().getId().toString());
        return storePodClient.create(newRequest).map(it -> {
            internalStoreService.syncInStore(store.id());
            return it;
        }).then();
    }

    @Override
    public Mono<PageImpl<Object>> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        Page<ManagerStoreDto> internalStores = internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
        Mono<List<Object>> listMono = Flux.fromIterable(internalStores.getContent())
                .flatMap(it ->
                        getStore(it.id())).collectList();
        return listMono.map(it -> managerStoreMappers.toPage(it, internalStores));
    }

    @Override
    public Mono<Object> getStore(ManagerStoreId managerStoreId) {
        return storePodClient.getStore(managerStoreId.getId().toString());
    }
}
