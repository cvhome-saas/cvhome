package com.asrevo.cvhome.uaa.web.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.domain.InvitationStatus;
import com.asrevo.cvhome.uaa.domain.UserStatus;
import com.asrevo.cvhome.uaa.dto.CreateResetLinkRequest;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.InvitationDto;
import com.asrevo.cvhome.uaa.dto.InviteUserRequest;
import com.asrevo.cvhome.uaa.dto.IssuedLink;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserCounts;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.dto.UserIdentityDto;
import com.asrevo.cvhome.uaa.dto.UserSearch;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.IdentityNotFoundException;
import com.asrevo.cvhome.uaa.errors.InvitationNotUsableException;
import com.asrevo.cvhome.uaa.errors.LastCredentialException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.errors.UserNotPendingException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;
import com.asrevo.cvhome.uaa.idp.UserIdentityService;
import com.asrevo.cvhome.uaa.invitation.InvitationService;
import com.asrevo.cvhome.uaa.invitation.PasswordResetService;
import com.asrevo.cvhome.uaa.service.AdminService;
import com.asrevo.cvhome.uaa.session.SessionAdminService;
import com.asrevo.cvhome.uaa.session.SessionSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Platform-wide user administration. Every method sits behind the super-admin gate the filter chain also enforces.
 *
 * <p>
 * Literal sub-paths ({@code counts}, {@code invitations}, {@code assignable-roles}, {@code exists}) are declared
 * beside {@code {id}}; Spring picks the literal, and {@code id} is a UUID so nothing else could bind to it anyway.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private static final String METADATA_PREFIX = "metadata[";

    private final AdminService adminService;

    private final SessionAdminService sessions;

    private final InvitationService invitations;

    private final PasswordResetService resets;

    private final UserIdentityService userIdentities;

    private static Map<String, String> extractMetadataFilters(Map<String, String> allParams) {
        Map<String, String> metadataFilters = new HashMap<>();
        allParams.forEach((k, v) -> {
            if (k.startsWith(METADATA_PREFIX)) {
                metadataFilters.put(k.substring(METADATA_PREFIX.length(), k.length() - 1), v);
            }
        });
        return metadataFilters;
    }

    /**
     * The list, filtered. {@code q} searches username, email and names; {@code status} and {@code role} narrow;
     * {@code metadata[key]=value} is the equality filter tenancy has always used. All optional, all ANDed.
     */
    @PreAuthorize(ADMIN)
    @GetMapping
    public Page<UserDto> users(@RequestParam(required = false) String q, @RequestParam(required = false) UserStatus status,
                               @RequestParam(required = false) String role, @RequestParam Map<String, String> allParams,
                               @PageableDefault Pageable pageable) {
        return adminService.getUsers(new UserSearch(q, status, role, extractMetadataFilters(allParams)), pageable);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/counts")
    public UserCounts counts() {
        return adminService.counts();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/{id}")
    public UserDto user(@PathVariable UUID id) throws UserNotFoundException {
        return adminService.getUser(id);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/exists")
    public boolean usernameExist(@RequestParam String username) {
        return adminService.usernameExist(username);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/enable")
    public void enable(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.enableUser(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/disable")
    public void disable(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.disableUser(id);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.delete(id);
    }

    @PreAuthorize(ADMIN)
    @PostMapping
    public UserDto create(@Valid @RequestBody CreateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException,
            PasswordPolicyViolationException, PasswordReusedException, PasswordCompromisedException, UsernameTakenException,
            EmailTakenException {
        return adminService.createUser(req);
    }

    @PreAuthorize(ADMIN)
    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException,
            EmailTakenException {
        return adminService.updateUser(id, req);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/email/verify")
    public UserDto verifyEmail(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        return adminService.verifyEmail(id);
    }

    // --- invitations and reset links ------------------------------------------------------------------------------

    @PreAuthorize(ADMIN)
    @GetMapping("/invitations")
    public Page<InvitationDto> invitations(@RequestParam(required = false) InvitationStatus status,
                                           @PageableDefault Pageable pageable) {
        return invitations.list(status, pageable);
    }

    /** Creates the account pending and answers with the link — the only response that ever carries it. */
    @PreAuthorize(ADMIN)
    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public IssuedLink invite(@Valid @RequestBody InviteUserRequest req)
            throws UsernameTakenException, EmailTakenException, RoleNotFoundException, RoleNotAssignableException,
            UserNotFoundException, SuperAdminImmutableException {
        return invitations.invite(req);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/invitations/resend")
    @ResponseStatus(HttpStatus.CREATED)
    public IssuedLink resendInvitation(@PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException, UserNotPendingException {
        return invitations.resend(id);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("/{id}/invitations")
    public void revokeInvitation(@PathVariable UUID id)
            throws UserNotFoundException, SuperAdminImmutableException, InvitationNotUsableException {
        invitations.revoke(id);
    }

    /** Issues a reset link. The body is optional; {@code revokeSessions} signs the account out now rather than later. */
    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/password-reset-links")
    @ResponseStatus(HttpStatus.CREATED)
    public IssuedLink createResetLink(@PathVariable UUID id, @RequestBody(required = false) CreateResetLinkRequest req)
            throws UserNotFoundException, SuperAdminImmutableException {
        return resets.createLink(id, req != null && req.revokeSessions());
    }

    // --- passwords, lockout, sessions -----------------------------------------------------------------------------

    @PreAuthorize(ADMIN)
    @PutMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable UUID id, @RequestBody ResetUserPasswordRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, PasswordPolicyViolationException,
            PasswordReusedException, PasswordCompromisedException {
        adminService.resetPassword(id, req);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/unlock")
    public void unlock(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.unlock(id);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/{id}/sessions")
    public List<SessionSummary> sessions(@PathVariable UUID id) throws UserNotFoundException {
        return sessions.list(adminService.getUser(id).username(), null);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("/{id}/sessions/{sessionId}")
    public void revokeSession(@PathVariable UUID id, @PathVariable String sessionId)
            throws UserNotFoundException, SessionNotFoundException {
        sessions.revoke(adminService.getUser(id).username(), sessionId);
    }

    /** Signs the account out everywhere. */
    @PreAuthorize(ADMIN)
    @DeleteMapping("/{id}/sessions")
    public Map<String, Integer> revokeSessions(@PathVariable UUID id) throws UserNotFoundException {
        return Map.of("revoked", sessions.revokeAll(adminService.getUser(id).username(), null));
    }

    // --- roles ----------------------------------------------------------------------------------------------------

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/roles")
    public void assign(@PathVariable UUID id, @RequestBody Set<String> roles)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        adminService.assignRoles(id, roles);
    }

    @PreAuthorize(ADMIN)
    @PostMapping("/{id}/roles/remove")
    public void removeRoles(@PathVariable UUID id, @RequestBody Set<String> roles)
            throws UserNotFoundException, SuperAdminImmutableException {
        adminService.removeRoles(id, roles);
    }

    /** The account's linked external identities. */
    @PreAuthorize(ADMIN)
    @GetMapping("/{id}/identities")
    public List<UserIdentityDto> identities(@PathVariable UUID id) throws UserNotFoundException {
        return userIdentities.list(adminService.getUser(id).id());
    }

    /** Unlinks one; refused when it is the account's only credential. */
    @PreAuthorize(ADMIN)
    @DeleteMapping("/{id}/identities/{identityId}")
    public void unlinkIdentity(@PathVariable UUID id, @PathVariable UUID identityId)
            throws UserNotFoundException, SuperAdminImmutableException, IdentityNotFoundException, LastCredentialException {
        userIdentities.unlink(adminService.getNonSuperAdmin(id), identityId);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/assignable-roles")
    public Set<String> assignableRoles() {
        return adminService.getAssignableRoles();
    }

}
