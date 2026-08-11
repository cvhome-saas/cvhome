package com.asrevo.cvhome.controlplane.manager.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.controlplane.manager.service.InternalOrgService;
import com.asrevo.cvhome.controlplane.manager.service.SignupService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {

    private final UserAccountService userAccountService;

    private final InternalOrgService internalOrgService;

    @Override
    public ReadableUser createOrgUser(CreateOrgRequest request)
            throws UaaConflictException, UaaApiUnavailableException {
        ManagerOrgId org = internalOrgService.createOrgForUser(new Email(request.user().getEmailAddress()));
        request.user().setActive(true);
        request.user().setUserName(request.user().getEmailAddress());
        request.user().setOrg(org.id().toString());
        request.user().setRoles(Set.of("ORG_ADMIN"));
        return userAccountService.createUser(request.user());
    }

}
