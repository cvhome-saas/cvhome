package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;

public interface SignupService {

    ReadableUser createOrgUser(CreateOrgRequest request);

}
