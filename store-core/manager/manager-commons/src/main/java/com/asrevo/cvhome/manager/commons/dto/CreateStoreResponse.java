package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;

public record CreateStoreResponse(ManagerStoreDto storeDto, CreateReferenceResponse createReferenceResponse) {
}
