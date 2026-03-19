import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ClientsService {

  private BASE_URL: string = `api/v1/admin/clients`;

  constructor(private httpClient: HttpClient) {
  }

  list(request: PageRequest): Observable<PageT<ClientSummary>> {
    return this.httpClient.get<PageT<ClientSummary>>(`${this.BASE_URL}`, {params: this.getParams(request)});
  }

  delete(clientId: string): Observable<any> {
    return this.httpClient.delete(`${this.BASE_URL}/${clientId}`);
  }

  findOne(clientId: string): Observable<any> {
    return this.httpClient.get(`${this.BASE_URL}/${clientId}`);
  }

  getParams(request: PageRequest): HttpParams {
    return new HttpParams({fromObject: {...request}});
  }

  save(value: any):Observable<ClientSummary> {
    return this.httpClient.post<ClientSummary>(`${this.BASE_URL}`, value);
  }

  update(id:string,value: any):Observable<ClientSummary> {
    return this.httpClient.put<ClientSummary>(`${this.BASE_URL}/${id}`, value);
  }

  getOptions(): Observable<any> {
    return this.httpClient.get(`${this.BASE_URL}/options`);
  }

  resetSecret(id: string, newSecret: string): Observable<any> {
    return this.httpClient.post(`${this.BASE_URL}/${id}/reset-secret`, {newSecret});
  }
}

export interface ClientSummary {
  id: string
  clientId: string
  clientName: string
}
