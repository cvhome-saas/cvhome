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
    return this.crudService.get(`/control-plane/api/v1/user-account/current`);
  }

  createUser(user: any,store:string): Observable<any> {
    return this.crudService.post(`/control-plane/api/v1/user-account/create`, user,{store});
  }

  updateUser(user: any,store:string): Observable<any> {
    return this.crudService.put(`/control-plane/api/v1/user-account/update`, user,{store});
  }

  getUser(userId: any): Observable<any> {
    return this.crudService.get(`/control-plane/api/v1/user-account/find-one?userId=${userId}`);
  }

  groups(): Observable<string[]> {
    return this.crudService.get(`/control-plane/api/v1/user-account/groups`);
  }

  getUsersList(params): Observable<any> {
    return this.crudService.get(`/control-plane/api/v1/user-account/list`, params);
  }

  deleteUser(userId: string, store: string): Observable<any> {
    return this.crudService.delete(`/control-plane/api/v1/user-account/delete?userId=${userId}`, {store});
  }

  disable(userId: string, store: string) {
    return this.crudService.post(`/control-plane/api/v1/user-account/disable?userId=${userId}`, {});
  }

  enable(userId: string, store: string) {
    return this.crudService.post(`/control-plane/api/v1/user-account/enable?userId=${userId}`, {store});
  }
}
