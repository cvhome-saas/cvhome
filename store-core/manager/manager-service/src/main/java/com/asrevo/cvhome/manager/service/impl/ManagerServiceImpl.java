package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.service.StoreManagerService;
import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ManagerServiceImpl implements StoreManagerService {
    private final RouterService routerService;
    private final InternalStoreService internalStoreService;
    private final static String baseDomain = ".cvhome.click";


    @Override
    public CreateStoreResponse createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId) {
        ManagerStoreDto store = internalStoreService.createStore(storeRequest, identityId);
        CreateReferenceResponse referenceResponse = createReference(storeRequest, store);
        internalStoreService.syncInRouter(store.id());
        return new CreateStoreResponse(store, referenceResponse);
    }

    private CreateReferenceResponse createReference(CreateManagerStoreRequest storeRequest, ManagerStoreDto store) {
        DomainReference reference = new DomainReference(store.id().toString());
        Domain suggestedSubDomain = new Domain(storeRequest.name() + baseDomain);
        CreateNewReferenceDto createNewReferenceDto = new CreateNewReferenceDto(reference, suggestedSubDomain, storeRequest.country());
        return routerService.create(createNewReferenceDto);
    }
}
