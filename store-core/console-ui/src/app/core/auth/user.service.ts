import {Injectable, inject} from '@angular/core';

import {Observable} from 'rxjs';
import {CrudService} from '../http/crud.service';
import {PersistableUser, User} from '@models/user';
import {PageT, StorePageRequest} from '../table/table.types';
import {BrowserStorage} from '../platform/browser-storage';

/** The uaa AuthController#me endpoint returns the raw Spring Security
 *  principal as `Object` — genuinely untyped on the Java side. Only `id`
 *  is read here. */
export interface AuthPrincipal {
  id?: string;
}

/** Base path for tenancy's user-account endpoints. */
export const USER_ACCOUNT_API_BASE = '/tenancy/api/v1/user-account';

@Injectable({
  providedIn: 'root'
})

export class UserService {
  private readonly crudService = inject(CrudService);
  private readonly storage = inject(BrowserStorage);
  userIdString = 'userId';


  getUserProfile(): Observable<AuthPrincipal> {
    return this.crudService.get(`/api/v1/auth/me`);
  }

  /** No matching controller was found for this endpoint — typed from the
   *  request body change-password.facade.ts already constructs. */
  updatePassword(id: string, passwords: {changePassword: string; password: string}): Observable<void> {
    return this.crudService.patch(`/v1/private/user/${id}/password`, passwords);
  }

  getUserId(): string {
    return this.storage.getItem(this.userIdString) ?? '';
  }


  getCurrentAccount(): Observable<User> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/current`);
  }

  createUser(user: PersistableUser, store: string): Observable<User> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/create`, user, {store});
  }

  updateUser(user: PersistableUser, store: string): Observable<User> {
    return this.crudService.put(`${USER_ACCOUNT_API_BASE}/update`, user, {store});
  }

  getUser(userId: string): Observable<User> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/find-one?userId=${userId}`);
  }

  roles(): Observable<string[]> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/assignable-roles`);
  }

  getUsersList(params: StorePageRequest): Observable<PageT<User>> {
    return this.crudService.get(`${USER_ACCOUNT_API_BASE}/list`, params);
  }

  deleteUser(userId: string, store: string): Observable<void> {
    return this.crudService.delete(`${USER_ACCOUNT_API_BASE}/delete?userId=${userId}`, {store});
  }

  disable(userId: string, store: string): Observable<void> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/disable?userId=${userId}`, {store});
  }

  enable(userId: string, store: string): Observable<void> {
    return this.crudService.post(`${USER_ACCOUNT_API_BASE}/enable?userId=${userId}`, {store});
  }
}
