package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.CreatedInvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgMemberDto;
import com.asrevo.cvhome.tenancy.errors.InvitationAlreadyExistsException;
import com.asrevo.cvhome.tenancy.errors.InvitationNotUsableException;
import com.asrevo.cvhome.tenancy.manager.service.InvitationService;
import com.asrevo.cvhome.tenancy.manager.service.OrgMemberService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

/**
 * Who belongs to an organization, and who has been invited to.
 *
 * <p>
 * Everything except {@code accept} is scoped to the caller's own organization: the org id comes from the
 * identity, never from a parameter, so an org admin cannot address someone else's organization by changing a
 * query string. A super admin acting on another org goes through the admin controller.
 * </p>
 */
@RestController
@RequestMapping("api/v1/org-member")
@AllArgsConstructor
@Tag(name = "Organization members", description = "Members and invitations")
public class OrgMemberApi {

    private static final String ORG_ADMIN = "hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')";

    private final OrgMemberService memberService;

    private final InvitationService invitationService;

    @GetMapping("list")
    @PreAuthorize(ORG_ADMIN)
    public List<OrgMemberDto> members(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        return memberService.list(identity.org());
    }

    @DeleteMapping
    @PreAuthorize(ORG_ADMIN)
    public Map<String, Boolean> remove(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                       @RequestParam String userId, Authentication authentication) {
        return Map.of("removed", memberService.remove(identity.org(), userId, actorOf(authentication)));
    }

    @GetMapping("invitations")
    @PreAuthorize(ORG_ADMIN)
    public List<InvitationDto> invitations(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
        return invitationService.list(identity.org());
    }

    /**
     * Creates an invitation and returns its token <strong>once</strong>.
     *
     * <p>
     * Nothing emails it — this platform has no mail sender — so the console shows a link for the admin to send.
     * The response is the only time the token is readable; only its hash is stored.
     * </p>
     */
    @PostMapping("invitations")
    @PreAuthorize(ORG_ADMIN)
    public CreatedInvitationDto invite(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                       @RequestParam String email,
                                       @RequestParam(defaultValue = "STORE_ADMIN") String role,
                                       Authentication authentication) throws InvitationAlreadyExistsException {
        return invitationService.invite(identity.org(), email, role, actorOf(authentication));
    }

    /** Issues a fresh token and invalidates the previous one — a link that went astray should stop working. */
    @PostMapping("invitations/resend")
    @PreAuthorize(ORG_ADMIN)
    public CreatedInvitationDto resend(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                       @RequestParam String email,
                                       @RequestParam(defaultValue = "STORE_ADMIN") String role,
                                       Authentication authentication) throws InvitationAlreadyExistsException {
        return invitationService.resend(identity.org(), email, role, actorOf(authentication));
    }

    @PostMapping("invitations/revoke")
    @PreAuthorize(ORG_ADMIN)
    public InvitationDto revoke(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                @RequestParam String invitationId, Authentication authentication)
            throws InvitationNotUsableException {
        return invitationService.revoke(identity.org(), invitationId, actorOf(authentication));
    }

    /**
     * Accepts an invitation for the signed-in user.
     *
     * <p>
     * Authenticated but carries no permission token, deliberately: the invitee is not yet a member of the
     * organization, so no org-scoped check could pass. The bearer token in the link is the authorization, and it
     * decides which organization is joined — which is why it is random, hashed at rest and single-use.
     * </p>
     */
    @PostMapping("invitations/accept")
    public InvitationDto accept(@RequestParam String token, Authentication authentication)
            throws InvitationNotUsableException {
        return invitationService.accept(token, actorOf(authentication));
    }

    private static String actorOf(Authentication authentication) {
        return authentication == null ? "unknown" : authentication.getName();
    }

}
