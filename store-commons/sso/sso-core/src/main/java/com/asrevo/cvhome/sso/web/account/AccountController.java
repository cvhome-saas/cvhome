package com.asrevo.cvhome.sso.web.account;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.dto.MeResponse;
import com.asrevo.cvhome.sso.dto.UserIdentityDto;
import com.asrevo.cvhome.sso.idp.UserIdentityService;
import com.asrevo.cvhome.sso.security.CurrentUserResolver;
import com.asrevo.cvhome.sso.service.AccountService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionSummary;
import com.asrevo.cvhome.uaa.errors.CurrentPasswordMismatchException;
import com.asrevo.cvhome.uaa.errors.IdentityNotFoundException;
import com.asrevo.cvhome.uaa.errors.LastCredentialException;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * What a signed-in person may do to their own account. Any authenticated user; a service client is refused as not
 * a user.
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private final CurrentUserResolver currentUser;

    private final AccountService account;

    private final SessionAdminService sessions;

    private final UserIdentityService identities;

    @GetMapping("me")
    public MeResponse me(Authentication authentication) throws NotAUserPrincipalException {
        return currentUser.describe(authentication);
    }

    /** Changes the password, then ends every other session and every token of the account. */
    @PutMapping("password")
    public void changePassword(@RequestBody ChangePasswordRequest req, Authentication authentication, HttpSession session)
            throws NotAUserPrincipalException, CurrentPasswordMismatchException, PasswordPolicyViolationException,
            PasswordReusedException, PasswordCompromisedException {
        account.changePassword(currentUser.resolve(authentication), req.currentPassword(), req.newPassword(),
                session == null ? null : session.getId());
    }

    @GetMapping("sessions")
    public List<SessionSummary> sessions(Authentication authentication, HttpSession session)
            throws NotAUserPrincipalException {
        return sessions.list(currentUser.resolve(authentication).getUsername(), session == null ? null : session.getId());
    }

    @DeleteMapping("sessions/{sessionId}")
    public void revokeSession(@PathVariable String sessionId, Authentication authentication)
            throws NotAUserPrincipalException, SessionNotFoundException {
        sessions.revoke(currentUser.resolve(authentication).getUsername(), sessionId);
    }

    /** The external logins linked to this account. */
    @GetMapping("identities")
    public List<UserIdentityDto> identities(Authentication authentication) throws NotAUserPrincipalException {
        return identities.list(currentUser.resolve(authentication).getId());
    }

    /** Unlinks one; refused when it is the account's only way to sign in. */
    @DeleteMapping("identities/{identityId}")
    public void unlinkIdentity(@PathVariable UUID identityId, Authentication authentication)
            throws NotAUserPrincipalException, IdentityNotFoundException, LastCredentialException {
        identities.unlink(currentUser.resolve(authentication), identityId);
    }

    /** Ends every session but this one. */
    @DeleteMapping("sessions")
    public Map<String, Integer> revokeOtherSessions(Authentication authentication, HttpSession session)
            throws NotAUserPrincipalException {
        return Map.of("revoked", sessions.revokeAll(currentUser.resolve(authentication).getUsername(),
                session == null ? null : session.getId()));
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {
    }

}
