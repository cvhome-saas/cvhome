package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.dto.IpodDto;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@AllArgsConstructor
public class StoreManagerServiceImpl implements StoreManagerService {
    private final SaasProperties saasProperties;
    private final RouterService routerService;
    private final RouterAllocationService routerAllocationService;
    private final InternalStoreService internalStoreService;
    private final StorePodClientFactory storePodClientFactory;

    @Override
    public CreateManagerStoreResponse createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId) {
        ManagerStoreDto store = internalStoreService.createStore(storeRequest, identityId);
        CreateReferenceResponse referenceResponse = createReference(storeRequest, store);
        internalStoreService.syncInRouter(store.id());
        CreateStoreResponse createStoreResponse = createStoreInStorePod(storeRequest, referenceResponse);
        internalStoreService.syncInStore(store.id());
        return new CreateManagerStoreResponse(store, referenceResponse);
    }

    @Override
    public Mono<PageImpl<Object>> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        Page<ManagerStoreDto> internalStores = internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
        Mono<List<Object>> listMono = Flux.fromIterable(internalStores.getContent())
                .flatMap((ManagerStoreDto managerStoreDto) -> getStore(managerStoreDto.id())).collectList();
        return listMono.map(it -> new PageImpl<>(it, internalStores.getPageable(), internalStores.getTotalElements()));
    }

    private Mono<Object> getStore(ManagerStoreId managerStoreId) {
        return routerAllocationService.getAllocation(new DomainReference(managerStoreId.getId().toString()))
                .flatMap(it -> getStorePodClient(it).getStore(it.reference()));
    }

    public Mono<Object> getStore(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId) {
        return routerAllocationService.getAllocation(new DomainReference(managerStoreId.getId().toString()))
                .flatMap(it -> getStorePodClient(it).getStore(it.reference()));
    }

    private CreateStoreResponse createStoreInStorePod(CreateManagerStoreRequest storeRequest, CreateReferenceResponse referenceResponse) {
        return getStorePodClient(referenceResponse.podDto()).create(new CreateStoreResponse(storeRequest.name()));
    }

    private StorePodClient getStorePodClient(IpodDto ipodDto) {
        return storePodClientFactory.createClient(ipodDto);
    }

    private CreateReferenceResponse createReference(CreateManagerStoreRequest storeRequest, ManagerStoreDto store) {
        DomainReference reference = new DomainReference(store.id().getId().toString());
        Domain suggestedSubDomain = new Domain(storeRequest.name() + "." + saasProperties.getDefaultDomain());
        CreateNewReferenceDto createNewReferenceDto = new CreateNewReferenceDto(reference, suggestedSubDomain, storeRequest.country());
        return routerService.create(createNewReferenceDto);
    }
}
