package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;
import com.asrevo.cvhome.tenancy.manager.service.SignupService;
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

    /**
     * Creates the organization and its first administrator, or neither.
     *
     * <p>
     * The transaction is the fix. The organization row was committed by {@code createOrgForUser} before uaa was
     * asked for anything, so a duplicate email — the single most likely way for a signup to fail — left an
     * organization with no user, no way in, and nothing to notice it. Nothing retried, because nothing had
     * failed as far as this service was concerned.
     * </p>
     *
     * <p>
     * Holding both in one transaction means a uaa refusal rolls the organization back. The remaining window is
     * narrower and the other way round: uaa accepts the user and the local commit then fails, leaving an orphaned
     * <em>user</em>. That is recoverable — the same email can be signed up again once the user is removed — where
     * an orphaned organization was not, and it does not silently look like success.
     * </p>
     */
    @Override
    @Transactional
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
