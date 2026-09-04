import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {
  AdminUserService,
  USER_API_BASE,
  toPlatformUserRow,
  type PlatformUserRow,
  type SessionSummary,
  type UserStatus,
} from '@cvhome-saas/ui-kit/uaa';

/**
 * This store's shoppers — the accounts that sign in to its storefront.
 *
 * The same client the platform console uses, pointed at cua instead of uaa: the contract is
 * identical because it is the same service behind both, so a second client would only be a way for
 * the two to drift. {@link SHOPPERS_PROVIDERS} is what re-points it, and the page provides that
 * rather than the application, so the platform's own account screens are untouched.
 *
 * **Narrower on purpose.** cua offers no create, no invite, no role assignment and no password
 * reset for a shopper: those accounts self-register, their roles are the deployment's configuration,
 * and resetting a password is the shopper's own flow from the storefront. The methods exist on the
 * shared client; this one does not expose them, and the server answers 404 to anything else.
 */
export const SHOPPERS_API_PATH = '/spg/cua/api/v1/private/shoppers';

/** What the list renders for one query. */
export interface ShoppersSnapshot {
  readonly rows: readonly PlatformUserRow[];
  readonly totalElements: number;
  readonly totalPages: number;
}

/** What the page asks for. `status` is exact; `q` matches username, email and name. */
export interface ShoppersQuery {
  readonly page: number;
  readonly count: number;
  readonly q: string;
  readonly status: UserStatus | '';
}

@Injectable()
export class ShoppersApi {
  private readonly shoppers = inject(AdminUserService);

  load(query: ShoppersQuery): Observable<ShoppersSnapshot> {
    return this.shoppers
      .search(query.page, query.count, {q: query.q, status: query.status || undefined})
      .pipe(
        map((page) => ({
          rows: (page.content ?? []).map(toPlatformUserRow),
          totalElements: page.totalElements,
          totalPages: page.totalPages,
        })),
      );
  }

  /** Ends every session and every token the account holds, as disabling always has. */
  disable(id: string): Observable<void> {
    return this.shoppers.disable(id);
  }

  enable(id: string): Observable<void> {
    return this.shoppers.enable(id);
  }

  /** Clears a lockout after too many wrong passwords. Idempotent. */
  unlock(id: string): Observable<void> {
    return this.shoppers.unlock(id);
  }

  sessions(id: string): Observable<readonly SessionSummary[]> {
    return this.shoppers.sessions(id);
  }

  revokeSessions(id: string): Observable<{revoked: number}> {
    return this.shoppers.revokeSessions(id);
  }

  /**
   * Removes the account and its credentials.
   *
   * The erasure path a merchant needs: they are the controller for their shoppers and this platform
   * the processor. Orders and invoices are the store's own records and live in other services.
   */
  delete(id: string): Observable<void> {
    return this.shoppers.delete(id);
  }
}


/** Provided by the page, so only the page's injector sees the re-pointed client. */
export const SHOPPERS_PROVIDERS = [
  {provide: USER_API_BASE, useValue: SHOPPERS_API_PATH},
  AdminUserService,
  ShoppersApi,
] as const;
