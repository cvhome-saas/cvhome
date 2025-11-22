import {Injectable} from '@angular/core';

import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SelectedStoreService} from "./selected-store.service";
import {Store} from "../models/commons";

@Injectable({
  providedIn: 'root'
})
export class CrudService {
  url = environment.apiUrl;

  constructor(private http: HttpClient, private selectedStoreService: SelectedStoreService) {
  }

  get(path, params?: any): Observable<any> {
    return this.http.get(`${this.url}${path}`, {responseType: 'json', params: this.getParams(params)});
  }

  post(path, body: any | null, params?: any): Observable<any> {
    return this.http.post(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  patch(path, body: any | null, params?: any) {
    return this.http.patch(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  put(path, body: any | null, params?: any): Observable<any> {
    return this.http.put(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  delete(path, params?: any): Observable<any> {
    return this.http.delete(`${this.url}${path}`, {responseType: 'json', params: this.getParams(params)});
  }


  getBaseUrl() {
    return `${this.url}`;
  }

  private getParams(p?: Record<string, string>): Record<string, string> {
    const params = p ? {...p} : {};
    let store: Store;

    if (p && p.store) {
      store = this.selectedStoreService.getStore(p.store);
    } else {
      store = this.selectedStoreService.currentSelectedStore();
    }


    if (store) {
      params['store'] = store.id.id;
      params['pod'] = store.podId.id;
    }

    return params;
  }

}
