import {Injectable, inject} from '@angular/core';

import {AuthService} from '@cvhome-saas/ui-kit';

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

  /**
   * Mirrors the guard every platform endpoint carries — `hasAnyRole('ROLE_SUPER_ADMIN')` on
   * `OrgManagerApi`'s writes and the two statistic controllers, `STORE-CORE.POD.MANAGE` on the pod
   * registry's writes, and `SCOPE_super_admin or ROLE_SUPER_ADMIN` on uaa's admin API.
   *
   * **Support too.** `ROLE_SUPPORT` is admitted to the reads it needs to find a merchant — the
   * organizations, one organization and its stores, uaa's account list — and to nothing else; the
   * rail it gets is the same, and the pages it cannot write to render the server's refusal. See
   * lessons.md, "Platform — impersonation".
   *
   * This decides what is *offered*. `platformOnly` decides what is reachable, and the server decides
   * what is permitted; hiding the rail changes none of the latter two.
   */
  canAdministerPlatform(): boolean {
    const roles = this.auth.getRoles();
    return roles.isSuperAdmin || roles.isSupport;
  }

  /**
   * Mirrors uaa's `users:impersonate`, which the seed grants to `SUPER_ADMIN` and `SUPPORT` and to
   * nobody else. The permission itself is in the token's `permissions` claim; the roles are the
   * fallback for a token minted before the claim carried it.
   */
  canImpersonate(): boolean {
    const account = this.auth.getCachedAuthUser();
    if (account?.permissions?.includes('users:impersonate')) {
      return true;
    }
    const roles = this.auth.getRoles();
    return roles.isSuperAdmin || roles.isSupport;
  }

  /** Whether this operator may pick write mode — uaa refuses it to support, so the dialog does not offer it. */
  canImpersonateInWriteMode(): boolean {
    return this.auth.getRoles().isSuperAdmin;
  }

  /**
   * Mirrors `STORE-POD.CONTENT.*` → `hasManageAccessOnStore`: org admin or store admin. A store
   * moderator has `STORE-POD.CONTENT.READ` and sees the lists read-only.
   */
  canManageContent(): boolean {
    const roles = this.auth.getRoles();
    return roles.isSuperAdmin || roles.isOrgAdmin || roles.isStoreAdmin;
  }
}
