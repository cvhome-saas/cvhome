import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class RolesService {
  private BASE_URL: string = `api/v1/admin/roles`;

  constructor(private httpClient: HttpClient) {
  }

  list(request: PageRequest): Observable<PageT<Role>> {
    return this.httpClient.get<PageT<Role>>(`${this.BASE_URL}`, {params: this.getParams(request)});
  }

  delete(roleId: string): Observable<any> {
    return this.httpClient.delete(`${this.BASE_URL}/${roleId}`);
  }

  findOne(roleId: string): Observable<any> {
    return this.httpClient.get(`${this.BASE_URL}/${roleId}`);
  }

  getParams(request: PageRequest): HttpParams {
    return new HttpParams({fromObject: {...request}});
  }

  save(value: any) :Observable<Role>{
    return this.httpClient.post<Role>(`${this.BASE_URL}`, value);
  }

  update(id:string,value: any):Observable<Role> {
    return this.httpClient.put<Role>(`${this.BASE_URL}/${id}`, value);
  }

}

export interface Role {
  id: string
  name: string
}
