package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.manager.service.InternalOrgService;
import com.asrevo.cvhome.manager.service.SignupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {
    private final UserAccountService userAccountService;
    private final InternalOrgService internalOrgService;

    @Override
    public ReadableUser createOrgUser(CreateOrgRequest request) {
        request.user().setActive(true);
        request.user().setUserName(request.user().getEmailAddress());
        ManagerOrgId org = internalOrgService.createOrgForUser(new Email(request.user().getEmailAddress()));
        return userAccountService.createOrgUser(org, request.user());
    }
}
