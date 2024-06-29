import {Injectable} from '@angular/core';

import {Observable} from 'rxjs';
import {roles} from '../models/access-roles';
import {CrudService} from './crud.service';

@Injectable({
  providedIn: 'root'
})

export class UserService {
  userIdString = 'userId';
  roles = {
    canAccessToOrder: false,
    isSuperadmin: false,
    isAdmin: false,
    isAdminCatalogue: false,
    isAdminStore: false,
    isAdminOrder: false,
    isAdminContent: false,
    isCustomer: false,
    isAdminRetail: false,
  };

  constructor(
    private crudService: CrudService
  ) {
  }


  getUserProfile(): Observable<any> {
    return this.crudService.get(`/api/v1/auth/me`);
  }

  checkIfUserExist(body): Observable<any> {
    return this.crudService.post(`/v1/private/user/unique`, body);
  }

  getMerchant(storeCode?): Observable<any> {
    return this.crudService.get(`/v1/store/${storeCode}`);
  }

  updateUserEnabled(user): Observable<any> {
    return this.crudService.patch(`/v1/private/user/${user.id}/enabled`, user);
  }

  // check roles for access to order page
  checkForAccess(array) {
    roles.forEach(role => {
      array.forEach(elem => {
        if (elem.name === role.name) {
          this.roles.canAccessToOrder = true;
        }
        switch (elem.name) {
          case 'SUPERADMIN':
            this.roles.isSuperadmin = true;
            break;
          case 'ADMIN':
            this.roles.isAdmin = true;
            break;
          case 'ADMIN_CATALOGUE':
            this.roles.isAdminCatalogue = true;
            break;
          case 'ADMIN_STORE':
            this.roles.isAdminStore = true;
            break;
          case 'ADMIN_ORDER':
            this.roles.isAdminOrder = true;
            break;
          case 'ADMIN_CONTENT':
            this.roles.isAdminContent = true;
            break;
          case 'CUSTOMER':
            this.roles.isCustomer = true;
            break;
          case 'ADMIN_RETAIL':
            this.roles.isAdminRetail = true;
            break;
        }
      });
    });
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

  getUsersList(store, params): Observable<any> {
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
