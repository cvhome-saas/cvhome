import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {CreatedInvitation, Invitation, OrgMember} from '@models/users';

/**
 * Not ported — **seller-core has no client for this at all**, and neither did any frontend.
 * Written against tenancy's `OrgMemberApi`, which is complete, unit-tested (`InvitationServiceTest`),
 * audited (`tenancy.tenancy_audit`) and until now called by nothing.
 *
 * Who belongs to an organization, and who has been asked to.
 *
 * The org is taken from the caller's identity on every method and never from a parameter, so an org
 * admin cannot address another organization by editing a query string. Membership is org-scoped, a
 * level above the store the rest of the console works in, so the `?store=` and `?pod=` that
 * `CrudService` stamps on every request are ignored by the server here — and an invitee who has no
 * store yet simply sends neither, because `SelectedStoreRequestContext` answers `{}` when nothing is
 * selected. It *throws* in that state during SSR, which is why the accepting page is client-rendered.
 *
 * **Nothing on this platform sends email.** The token comes back once, in the response to whoever
 * created the invitation, and only its hash is stored — so the console has to show a link for the
 * operator to send by whatever means they already use. Losing it means issuing a new invitation, the
 * same property a password reset link has and for the same reason. See lessons.md, "Users — nothing
 * emails an invitation".
 */
export const ORG_MEMBER_API_BASE = '/tenancy/api/v1/org-member';

@Injectable({providedIn: 'root'})
export class OrgMemberService {
  private readonly crudService = inject(CrudService);

  /**
   * Everyone who **accepted an invitation** into the caller's organization.
   *
   * Not the team, despite the name. `tenancy.org_member` has one writer — `InvitationService.accept`
   * — so the founder is absent (they are `manager_org.owner_user_id`) and so is anyone an
   * administrator created directly. `OrgMemberService.add` exists and is called by nothing.
   *
   * The id is not joinable either: accept stores `authentication.getName()`, the username, while
   * every id-taking endpoint wants uaa's UUID.
   *
   * TODO(lessons.md): the member list is not the team — see lessons.md, "Users — the member list is
   * not the team". The Team tab reads `user-account/list`, which is the real answer for a store.
   */
  members(): Observable<OrgMember[]> {
    return this.crudService.get(`${ORG_MEMBER_API_BASE}/list`);
  }

  /** Removes someone from the organization. Does not delete their uaa account. */
  removeMember(userId: string): Observable<{removed: boolean}> {
    return this.crudService.delete(ORG_MEMBER_API_BASE, {userId});
  }

  /** Every invitation the organization has issued, in every status. Never carries a token. */
  invitations(): Observable<Invitation[]> {
    return this.crudService.get(`${ORG_MEMBER_API_BASE}/invitations`);
  }

  /**
   * Invites an address to join, and returns the token **once**.
   *
   * The email is normalised to lowercase by the server, and a second pending invitation to the same
   * address is a 409 `INVITATION.ALREADY_EXISTS` rather than a silent replacement — which is what
   * `resend` is for.
   */
  invite(email: string, role: string): Observable<CreatedInvitation> {
    return this.crudService.post(`${ORG_MEMBER_API_BASE}/invitations`, {}, {email, role});
  }

  /**
   * Issues a fresh token and invalidates the previous one.
   *
   * Deliberately not "show me the old link again", which is impossible — only the hash is stored. A
   * link that went astray should stop working, so resending is a rotation.
   */
  resend(email: string, role: string): Observable<CreatedInvitation> {
    return this.crudService.post(`${ORG_MEMBER_API_BASE}/invitations/resend`, {}, {email, role});
  }

  /** Stops a pending invitation being usable. Recorded as `REVOKED` rather than deleted. */
  revoke(invitationId: string): Observable<Invitation> {
    return this.crudService.post(`${ORG_MEMBER_API_BASE}/invitations/revoke`, {}, {invitationId});
  }

  /**
   * Accepts an invitation for the signed-in user.
   *
   * The only method here that is authenticated and carries no permission token, deliberately: the
   * invitee is not a member of the organization yet, so no org-scoped check could pass. **The bearer
   * token in the link is the authorization**, and it decides which organization is joined — which is
   * why it is random, hashed at rest and single-use.
   *
   * This is also why the accepting page cannot sit inside the console shell: `consoleContext` and
   * `requiresStore` would both refuse someone who has no store yet.
   */
  accept(token: string): Observable<Invitation> {
    return this.crudService.post(`${ORG_MEMBER_API_BASE}/invitations/accept`, {}, {token});
  }
}
