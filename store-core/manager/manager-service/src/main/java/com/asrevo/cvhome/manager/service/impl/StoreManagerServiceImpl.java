package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoreManagerServiceImpl implements StoreManagerService {
    private final SaasProperties saasProperties;
    private final RouterService routerService;
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

    private CreateStoreResponse createStoreInStorePod(CreateManagerStoreRequest storeRequest, CreateReferenceResponse referenceResponse) {
        StorePodClient podClient = storePodClientFactory.createClient(referenceResponse.podDto());
        return podClient.create(new CreateStoreResponse(storeRequest.name()));
    }

    private CreateReferenceResponse createReference(CreateManagerStoreRequest storeRequest, ManagerStoreDto store) {
        DomainReference reference = new DomainReference(store.id().getId().toString());
        Domain suggestedSubDomain = new Domain(storeRequest.name() + "." + saasProperties.getDefaultDomain());
        CreateNewReferenceDto createNewReferenceDto = new CreateNewReferenceDto(reference, suggestedSubDomain, storeRequest.country());
        return routerService.create(createNewReferenceDto);
    }
}
