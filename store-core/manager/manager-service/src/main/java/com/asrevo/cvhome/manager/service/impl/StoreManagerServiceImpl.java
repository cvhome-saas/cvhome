package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.dto.IpodDto;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StoreManagerServiceImpl implements StoreManagerService {
    private final SaasProperties saasProperties;
    private final RouterService routerService;
    private final RouterAllocationService routerAllocationService;
    private final InternalStoreService internalStoreService;
    private final StorePodClientFactory storePodClientFactory;

    @Override
    public Mono<Void> createStore(IdentityId identityId, Map<Object, Object> request) {
        CreateManagerStoreRequest storeRequest = buildInternalCreateRequest(request);
        ManagerStoreDto store = internalStoreService.createStore(storeRequest, identityId);
        return createReference(storeRequest, store)
                .map(it -> {
                    internalStoreService.syncInRouter(store.id());
                    return it;
                })
                .flatMap(it -> createStoreInStorePod(request, it).map(x -> {
                    internalStoreService.syncInStore(store.id());
                    return x;
                }))
                .then();
    }

    private CreateManagerStoreRequest buildInternalCreateRequest(Map<Object, Object> request) {
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        String phone = (String) request.get("phone");
        Object address = request.get("address");
        String country = Optional.ofNullable(address)
                .map(it -> ((Map<String, String>) it))
                .map(it -> it.get("country"))
                .orElse(null);
        return new CreateManagerStoreRequest(name, new Phone(phone), Country.valueOf(country), new Email(email));
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

    private Mono<CreateStoreResponse> createStoreInStorePod(Map<Object, Object> request, CreateReferenceResponse response) {
        Map<Object, Object> newRequest = buildExternalCreateRequest(request, response.referenceDto().reference());
        return getStorePodClient(response.podDto()).create(newRequest);
    }

    private Map<Object, Object> buildExternalCreateRequest(Map<Object, Object> request, DomainReference reference) {
        HashMap<Object, Object> newRequest = new HashMap<>(request);
        newRequest.put("code", reference.reference());
        return newRequest;
    }

    private StorePodClient getStorePodClient(IpodDto ipodDto) {
        return storePodClientFactory.createClient(ipodDto);
    }

    private Mono<CreateReferenceResponse> createReference(CreateManagerStoreRequest storeRequest, ManagerStoreDto store) {
        DomainReference reference = new DomainReference(store.id().getId().toString());
        Domain suggestedSubDomain = new Domain(storeRequest.name() + "." + saasProperties.getDefaultDomain());
        CreateNewReferenceDto createNewReferenceDto = new CreateNewReferenceDto(reference, suggestedSubDomain, storeRequest.country());
        return routerService.create(createNewReferenceDto);
    }
}
