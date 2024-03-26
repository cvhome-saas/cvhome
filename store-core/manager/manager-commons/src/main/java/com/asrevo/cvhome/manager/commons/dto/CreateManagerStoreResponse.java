package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;

public record CreateManagerStoreResponse(ManagerStoreDto storeDto, CreateReferenceResponse createReferenceResponse) {
}
