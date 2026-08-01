import {Injectable, inject} from '@angular/core';

import {Observable} from 'rxjs';
import {CrudService} from './crud.service';
import {PersistableUser, User} from '../models/user';
import {PageT, StorePageRequest} from '../table/table.types';

/** The uaa AuthController#me endpoint returns the raw Spring Security
 *  principal as `Object` — genuinely untyped on the Java side. Only `id`
 *  is read here. */
export interface AuthPrincipal {
  id?: string;
}

@Injectable({
  providedIn: 'root'
})

export class UserService {
  private readonly crudService = inject(CrudService);
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
    return localStorage.getItem(this.userIdString);
  }


  getCurrentAccount(): Observable<User> {
    return this.crudService.get(`/control-plane/api/v1/user-account/current`);
  }

  createUser(user: PersistableUser, store: string): Observable<User> {
    return this.crudService.post(`/control-plane/api/v1/user-account/create`, user, {store});
  }

  updateUser(user: PersistableUser, store: string): Observable<User> {
    return this.crudService.put(`/control-plane/api/v1/user-account/update`, user, {store});
  }

  getUser(userId: string): Observable<User> {
    return this.crudService.get(`/control-plane/api/v1/user-account/find-one?userId=${userId}`);
  }

  roles(): Observable<string[]> {
    return this.crudService.get(`/control-plane/api/v1/user-account/assignable-roles`);
  }

  getUsersList(params: StorePageRequest): Observable<PageT<User>> {
    return this.crudService.get(`/control-plane/api/v1/user-account/list`, params);
  }

  deleteUser(userId: string, store: string): Observable<void> {
    return this.crudService.delete(`/control-plane/api/v1/user-account/delete?userId=${userId}`, {store});
  }

  disable(userId: string, store: string): Observable<void> {
    return this.crudService.post(`/control-plane/api/v1/user-account/disable?userId=${userId}`, {store});
  }

  enable(userId: string, store: string): Observable<void> {
    return this.crudService.post(`/control-plane/api/v1/user-account/enable?userId=${userId}`, {store});
  }
}
