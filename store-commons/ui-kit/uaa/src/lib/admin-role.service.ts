import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, UI_KIT_CONFIG, type SpringPage} from '@cvhome-saas/ui-kit';

/**
 * uaa's role registry — the names `AdminUserService.assignRoles` grants, and what each one carries.
 *
 * Guarded exactly like the user API — `AppSecurityConfig` gates `/api/v1/admin/**` on
 * `SCOPE_super_admin`/`ROLE_SUPER_ADMIN` at the filter chain, and every method repeats it in
 * `@PreAuthorize`. Ids are UUIDs, so a wrong-shaped one is a 400 at binding rather than a 404.
 *
 * A role is a name plus data: a description, a scope, an optional parent it inherits from, and a
 * set of permission keys from the catalogue at `GET /permissions`. The name is still the authority
 * and the token claim — a **system role** (seeded by the platform) keeps its name and scope for
 * ever and cannot be deleted; its permissions and description stay editable.
 */
export const ADMIN_ROLE_API_PATH = '/api/v1/admin/roles';

export type RoleScope = 'REALM' | 'ORGANIZATION' | 'CLIENT';

/** One role, as `RoleDto` on the server. */
export interface RoleDto {
  readonly id: string;
  /** The authority string, e.g. `STORE_ADMIN`. Upper-case letters, digits and underscores. */
  readonly name: string;
  readonly description: string | null;
  readonly scope: RoleScope;
  readonly systemRole: boolean;
  readonly inheritsFromId: string | null;
  readonly inheritsFromName: string | null;
  /** What the role grants itself. */
  readonly permissions: readonly string[];
  /** Those plus everything the parent chain grants — what the token's `permissions` claim carries. */
  readonly effectivePermissions: readonly string[];
  /** Accounts holding the role directly. */
  readonly userCount: number;
  readonly createdAt: string;
  readonly updatedAt: string | null;
}

export interface CreateRoleRequest {
  readonly name: string;
  readonly description?: string | null;
  readonly scope?: RoleScope;
  readonly inheritsFromId?: string | null;
  readonly permissions?: readonly string[];
}

/** Partial: an absent field is left alone. `clearInheritsFrom` removes the parent. */
export interface UpdateRoleRequest {
  readonly name?: string;
  readonly description?: string | null;
  readonly scope?: RoleScope;
  readonly inheritsFromId?: string | null;
  readonly clearInheritsFrom?: boolean;
  readonly permissions?: readonly string[];
}

/** Kept for callers that only ever set a name. */
export type RoleNameRequest = Pick<CreateRoleRequest, 'name'>;

/** One catalogue entry. `group` is how the matrix is laid out. */
export interface PermissionDto {
  readonly key: string;
  readonly group: 'IDENTITY' | 'CLIENTS' | 'IDENTITY_PROVIDERS' | 'SYSTEM';
  readonly description: string;
}

@Injectable({providedIn: 'root'})
export class AdminRoleService {
  private readonly crudService = inject(CrudService);
  /** `/uaa/…` behind the gateway, `/api/…` on uaa itself. See {@link UiKitConfig.uaaBasePath}. */
  private readonly base = `${inject(UI_KIT_CONFIG).uaaBasePath}${ADMIN_ROLE_API_PATH}`;

  /**
   * A page of roles.
   *
   * `count`, not Spring's `size`: uaa depends on `store-commons:autoconfigure` like every other
   * service, so the platform-wide paging rename applies here too.
   */
  list(page: number, count: number): Observable<SpringPage<RoleDto>> {
    return this.crudService.get(this.base, {page, count});
  }

  findOne(id: string): Observable<RoleDto> {
    return this.crudService.get(`${this.base}/${id}`);
  }

  /** The catalogue every role picks from; static on the server, so it cannot drift from what a grant accepts. */
  permissions(): Observable<readonly PermissionDto[]> {
    return this.crudService.get(`${this.base}/permissions`);
  }

  create(request: CreateRoleRequest): Observable<RoleDto> {
    return this.crudService.post(this.base, request);
  }

  update(id: string, request: UpdateRoleRequest): Observable<RoleDto> {
    return this.crudService.put(`${this.base}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${this.base}/${id}`);
  }
}
