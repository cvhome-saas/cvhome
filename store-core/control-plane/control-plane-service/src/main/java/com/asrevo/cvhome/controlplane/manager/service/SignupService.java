package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;

public interface SignupService {

	ReadableUser createOrgUser(CreateOrgRequest request);

}
