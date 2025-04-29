import {Injectable} from '@angular/core';

import {Observable} from 'rxjs';
import {CrudService} from './crud.service';

@Injectable({
  providedIn: 'root'
})

export class UserService {
  userIdString = 'userId';

  constructor(
    private crudService: CrudService
  ) {
  }


  getUserProfile(): Observable<any> {
    return this.crudService.get(`/api/v1/auth/me`);
  }

  updatePassword(id: any, passwords: any): Observable<any> {
    return this.crudService.patch(`/v1/private/user/${id}/password`, passwords);
  }

  getUserId(): string {
    return localStorage.getItem(this.userIdString);
  }


  getCurrentAccount(): Observable<any> {
    return this.crudService.get(`/manager/api/v1/user-account/current`);
  }

  createUser(user: any, store: any): Observable<any> {
    return this.crudService.post(`/manager/api/v1/user-account/create?store=${store}`, user);
  }

  updateUser(user: any, store: any): Observable<any> {
    return this.crudService.put(`/manager/api/v1/user-account/update?store=${store}`, user);
  }

  getUser(userId: any): Observable<any> {
    return this.crudService.get(`/manager/api/v1/user-account/find-one?userId=${userId}`);
  }

  groups(): Observable<string[]> {
    return this.crudService.get(`/manager/api/v1/user-account/groups`);
  }

  getUsersList(params): Observable<any> {
    return this.crudService.get(`/manager/api/v1/user-account/list`, params);
  }

  deleteUser(userId: any, store: any): Observable<any> {
    return this.crudService.delete(`/manager/api/v1/user-account/delete?store=${store}&userId=${userId}`, {});
  }

  disable(userId, store: string) {
    return this.crudService.post(`/manager/api/v1/user-account/disable?store=${store}&userId=${userId}`, {});
  }

  enable(userId, store: string) {
    return this.crudService.post(`/manager/api/v1/user-account/enable?store=${store}&userId=${userId}`, {});
  }
}
