import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private BASE_URL: string = `api/v1/admin/users`;

  constructor(private httpClient: HttpClient) {
  }

  list(request: PageRequest): Observable<PageT<User>> {
    return this.httpClient.get<PageT<User>>(`${this.BASE_URL}`, {params: this.getParams(request)});
  }

  delete(userId: string): Observable<any> {
    return this.httpClient.delete(`${this.BASE_URL}/${userId}`);
  }

  findOne(userId: string): Observable<any> {
    return this.httpClient.get(`${this.BASE_URL}/${userId}`);
  }

  getParams(request: PageRequest): HttpParams {
    return new HttpParams({fromObject: {...request}});
  }

  save(value: any): Observable<User> {
    return this.httpClient.post<User>(`${this.BASE_URL}`, value);
  }

  update(id: string, value: any): Observable<User> {
    return this.httpClient.put<User>(`${this.BASE_URL}/${id}`, value);
  }

  resetPassword(id: string, password: any): Observable<User> {
    return this.httpClient.put<User>(`${this.BASE_URL}/${id}/reset-password`, {password});
  }

  assignRoles(id: string, roles: string[]): Observable<void> {
    return this.httpClient.post<void>(`${this.BASE_URL}/${id}/roles`, roles);
  }

  removeRoles(id: string, roles: string[]): Observable<void> {
    return this.httpClient.post<void>(`${this.BASE_URL}/${id}/roles/remove`, roles);
  }

}

export interface User {
  id: string
  username: string
  email: string
  roles: string[]
  metadata: { [key: string]: any }
}
