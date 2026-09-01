import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService, type SpringPage} from '@cvhome-saas/ui-kit';

/**
 * uaa's role registry — the names `AdminUserService.assignRoles` grants.
 *
 * The first client for `AdminRoleController`, which has been complete and guarded since before
 * console-ui existed and had no caller: uaa's own admin SPA predates it, and the console reads the
 * *assignable* list from `/users/assignable-roles` rather than administering roles itself.
 *
 * Guarded exactly like the user API — `AppSecurityConfig` gates `/api/v1/admin/**` on
 * `SCOPE_super_admin`/`ROLE_SUPER_ADMIN` at the filter chain, and every method repeats it in
 * `@PreAuthorize`. Ids are UUIDs, so a wrong-shaped one is a 400 at binding rather than a 404.
 *
 * A role is a name and nothing else: `Role` is `{id, name}`, and both request records carry the
 * single field. Permissions are not modelled here — authorities are the role names themselves.
 */
export const ADMIN_ROLE_API_BASE = '/uaa/api/v1/admin/roles';

/** One role. `name` is the authority string, e.g. `ROLE_STORE_ADMIN`. */
export interface RoleDto {
  readonly id: string;
  readonly name: string;
}

export interface RoleNameRequest {
  readonly name: string;
}

@Injectable({providedIn: 'root'})
export class AdminRoleService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of roles.
   *
   * `count`, not Spring's `size`: uaa depends on `store-commons:autoconfigure` like every other
   * service, so the platform-wide paging rename applies here too.
   */
  list(page: number, count: number): Observable<SpringPage<RoleDto>> {
    return this.crudService.get(ADMIN_ROLE_API_BASE, {page, count});
  }

  findOne(id: string): Observable<RoleDto> {
    return this.crudService.get(`${ADMIN_ROLE_API_BASE}/${id}`);
  }

  create(request: RoleNameRequest): Observable<RoleDto> {
    return this.crudService.post(ADMIN_ROLE_API_BASE, request);
  }

  /** `PUT /{id}` — note the leading slash on the controller's mapping, unlike its siblings. */
  update(id: string, request: RoleNameRequest): Observable<RoleDto> {
    return this.crudService.put(`${ADMIN_ROLE_API_BASE}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${ADMIN_ROLE_API_BASE}/${id}`);
  }
}
