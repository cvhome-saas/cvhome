import {Injectable} from '@angular/core';

import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CrudService {
  url = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  get(path, params?: any): Observable<any> {
    return this.http.get(`${this.url}${path}`, {responseType: 'json', params});
  }

  getBaseUrl() {
    return `${this.url}`;
  }

  getWithEmpty(path, params?: { [param: string]: string | string[]; }): Observable<any> {
    return this.http.get(`${this.url}${path}`, {responseType: 'json', params}).pipe(catchError(error => of(error)))
  }

  post(path, body: any | null, options?: any): Observable<any> {
    return this.http.post(`${this.url}${path}`, body, options);
  }

  postWithStoreParam(path, body: any | null, storeCode, options?: any): Observable<any> {
    if (storeCode) {
      path = path + '?store=' + storeCode;
    }
    return this.http.post(`${this.url}${path}`, body, options);
  }

  patch(path, body: any | null, options?: any) {
    return this.http.patch(`${this.url}${path}`, body, options);
  }

  put(path, body: any | null, options?: any): Observable<any> {
    return this.http.put(`${this.url}${path}`, body, options);
  }

  delete(path, options?: any): Observable<any> {
    return this.http.delete(`${this.url}${path}`, options);
  }


}
