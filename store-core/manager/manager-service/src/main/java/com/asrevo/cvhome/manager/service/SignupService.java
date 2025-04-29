package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.manager.dto.CreateOrgRequest;

public interface SignupService {
    ReadableUser createOrgUser(CreateOrgRequest request);
}
