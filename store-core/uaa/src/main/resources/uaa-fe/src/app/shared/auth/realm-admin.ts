import {inject} from '@angular/core';
import {Router, type CanActivateFn, type CanMatchFn} from '@angular/router';
import {map} from 'rxjs';

import {AuthService} from '@cvhome-saas/ui-kit';

/**
 * Who this console is for.
 *
 * **The rule is the API's, not an invention here.** Every `/api/v1/admin/**` endpoint is behind
 * `hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')`, so an org or store administrator —
 * who holds real permissions like `users:read` in their token, and uses them in the seller console —
 * can read nothing at all through this one. Gating the rail on those permissions would offer screens
 * that answer 403, which is what this console did: a store moderator signed in, landed on an admin
 * screen and met an access-denied bar with a full navigation rail behind it.
 *
 * So the console asks the question the server will ask. When the admin API grows a permission-based
 * gate, this is the single place that changes.
 */
export function isRealmAdmin(auth: AuthService): boolean {
  return auth.getRoles().isSuperAdmin || auth.getCachedAuthUser()?.authorities.includes('SCOPE_super_admin') === true;
}

/**
 * Sends anyone else to their own account page rather than to a screen that will refuse them.
 *
 * `canAccessSecuredPages` has already fetched and cached `/me` by the time this runs, so the answer
 * needs no request of its own.
 */
export const canAdministerRealm: CanActivateFn = () => {
  const auth = inject(AuthService);
  return isRealmAdmin(auth) ? true : inject(Router).parseUrl('/account');
};

/**
 * Where signing in lands: the console for an administrator, their own account for everyone else.
 *
 * A `canMatch` rather than a redirect function, because a redirect is resolved while the URL is being
 * matched — before the parent's `canAccessSecuredPages` has fetched `/me`. Asked then, every visitor
 * looks like a non-administrator, and a super admin opening `/` was sent to their own account.
 * Matching guards may return an observable, so this one waits for the principal it is asking about.
 */
export const isAdminLanding: CanMatchFn = () => {
  const auth = inject(AuthService);
  return auth.getAuthUser().pipe(map(() => isRealmAdmin(auth)));
};
