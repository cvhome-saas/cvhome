package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.dto.IpodDto;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.asrevo.cvhome.s2s.utils.StoreCallUtils.getPodReference;
import static com.asrevo.cvhome.s2s.utils.StoreCallUtils.getStoreUrl;

@Service
public class StoreManagerServiceImpl implements StoreManagerService {
    private final SaasProperties saasProperties;
    private final RouterService routerService;
    private final RouterAllocationService routerAllocationService;
    private final InternalStoreService internalStoreService;
    private final StorePodClientFactory storePodClientFactory;
    private final ManagerStoreMappers managerStoreMappers;
    private final ServiceDomainProperties serviceDomainProperties;
    private final String fallbackStoreUrl;

    public StoreManagerServiceImpl(SaasProperties saasProperties, RouterService routerService, RouterAllocationService routerAllocationService, InternalStoreService internalStoreService, StorePodClientFactory storePodClientFactory, ManagerStoreMappers managerStoreMappers, ServiceDomainProperties serviceDomainProperties) {
        this.saasProperties = saasProperties;
        this.routerService = routerService;
        this.routerAllocationService = routerAllocationService;
        this.internalStoreService = internalStoreService;
        this.storePodClientFactory = storePodClientFactory;
        this.managerStoreMappers = managerStoreMappers;
        this.serviceDomainProperties = serviceDomainProperties;
        this.fallbackStoreUrl = getStoreUrl(serviceDomainProperties);
    }

    @Override
    public Mono<Void> createStore(IdentityId identityId, Map<Object, Object> request) {
        CreateManagerStoreRequest storeRequest = managerStoreMappers.toCreateStoreRequest(request);
        ManagerStoreDto store = internalStoreService.createStore(storeRequest, identityId);
        return createReference(storeRequest, store)
                .map(it -> {
                    internalStoreService.syncInRouter(store.id());
                    return it;
                })
                .flatMap(it -> createStoreInStorePod(request, it))
                .map(it -> {
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
    public Mono<Object> getStore(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId) {
        return getPodReferenceDto(managerStoreId).flatMap(it -> getStorePodClient(it).getStore(it.reference()));
    }


    private Mono<Object> getStore(ManagerStoreId managerStoreId) {
        return getPodReferenceDto(managerStoreId).flatMap(it -> getStorePodClient(it).getStore(it.reference()));
    }

    private Mono<PodReferenceDto> getPodReferenceDto(ManagerStoreId managerStoreId) {
        DomainReference reference = new DomainReference(managerStoreId.getId().toString());
        return getPodReference(serviceDomainProperties, routerAllocationService.getAllocation(reference), () -> buildFallbackPodReference(reference));
    }

    private PodReferenceDto buildFallbackPodReference(DomainReference reference) {
        return PodReferenceDto.from(this.fallbackStoreUrl, reference.reference());
    }

    private Mono<CreateStoreResponse> createStoreInStorePod(Map<Object, Object> request, PodReferenceDto podReferenceDto) {
        Map<Object, Object> newRequest = managerStoreMappers.toExternalCreateRequest(request, podReferenceDto.reference());
        return getStorePodClient(podReferenceDto).create(newRequest);
    }

    private StorePodClient getStorePodClient(IpodDto ipodDto) {
        return storePodClientFactory.createClient(ipodDto);
    }

    private Mono<PodReferenceDto> createReference(CreateManagerStoreRequest storeRequest, ManagerStoreDto store) {
        String reference = store.id().getId().toString();
        return getPodReference(serviceDomainProperties, Mono.defer(() -> {
            Domain suggestedSubDomain = new Domain(storeRequest.name() + "." + saasProperties.getDefaultDomain());
            CreateNewReferenceDto referenceDto = new CreateNewReferenceDto(new DomainReference(reference), suggestedSubDomain, storeRequest.country());
            return routerService.create(referenceDto)
                    .map(managerStoreMappers::toPodReferenceDto);
        }), () ->
                PodReferenceDto.from(this.fallbackStoreUrl, reference));
    }
}
