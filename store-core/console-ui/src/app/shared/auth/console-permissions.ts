import {Injectable, inject} from '@angular/core';

import {AuthService} from '@core/auth/auth.service';

/**
 * What the signed-in operator may do, as the console understands it.
 *
 * `AuthService.getRoles()` has existed since Module 1 and **nothing called it** — every page so far
 * either needed no gate or let the server refuse. The user page is the first that should: a store
 * moderator can read the team (`STORE-CORE.USERS.LIST` resolves to the store's read audience) and
 * can change none of it (`CREATE`/`UPDATE`/`DELETE`/`ENABLE`/`DISABLE`/`RESET_PASSWORD` all resolve
 * to `hasMaintainAccessOnUsers`, which is org admin or store admin). Rendering buttons that are
 * certain to 403 is worse than not rendering them.
 *
 * **This mirrors the server; it does not replace it.** Every one of those endpoints is guarded by
 * `@PreAuthorize` and re-checks the caller's org and store, so hiding a button changes what is
 * offered and not what is permitted. It is safe for exactly that reason: the answer is read from
 * token claims the client cannot mint.
 *
 * `getRoles()` reads a value `canAccessSecuredPages` has already fetched and cached, so this costs
 * no request — but it answers "no roles" before that guard has run, which is why it belongs on a
 * guarded route and nowhere else.
 */
@Injectable({providedIn: 'root'})
export class ConsolePermissions {
  private readonly auth = inject(AuthService);

  /** Mirrors `PermissionAccessChecker.hasMaintainAccessOnUsers` — org admin or store admin. */
  canManageUsers(): boolean {
    const roles = this.auth.getRoles();
    return roles.isSuperAdmin || roles.isOrgAdmin || roles.isStoreAdmin;
  }

  /**
   * Mirrors `OrgMemberApi`'s class-wide `hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')`.
   *
   * Narrower than `canManageUsers` on purpose: membership is org-scoped, so a **store** admin can
   * create accounts in their own store and cannot invite anyone into the organization.
   */
  canManageInvitations(): boolean {
    const roles = this.auth.getRoles();
    return roles.isSuperAdmin || roles.isOrgAdmin;
  }
}
