import {Injectable, inject} from '@angular/core';

import {Observable} from 'rxjs';
import {HttpClient, HttpEvent, HttpParams, HttpRequest} from '@angular/common/http';
import {UI_KIT_CONFIG} from '../config/ui-kit.config';
import {REQUEST_CONTEXT, RequestContextProvider} from './request-context';

export type HttpParamsLike = Record<string, string | number | boolean | undefined>;

export interface RequestOptions {
  params?: HttpParamsLike;
  reportProgress?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CrudService {
  private readonly http = inject(HttpClient);
  private readonly requestContext = inject<RequestContextProvider>(REQUEST_CONTEXT);
  private readonly config = inject(UI_KIT_CONFIG);

  url = this.config.apiUrl;

  get<T>(path: string, params?: HttpParamsLike): Observable<T> {
    return this.http.get<T>(`${this.url}${path}`, {responseType: 'json', params: this.getParams(params)});
  }

  post<T, B = unknown>(path: string, body: B | null, params?: HttpParamsLike): Observable<T> {
    return this.http.post<T>(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  patch<T, B = unknown>(path: string, body: B | null, params?: HttpParamsLike): Observable<T> {
    return this.http.patch<T>(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  put<T, B = unknown>(path: string, body: B | null, params?: HttpParamsLike): Observable<T> {
    return this.http.put<T>(`${this.url}${path}`, body, {responseType: 'json', params: this.getParams(params)});
  }

  delete<T>(path: string, params?: HttpParamsLike): Observable<T> {
    return this.http.delete<T>(`${this.url}${path}`, {responseType: 'json', params: this.getParams(params)});
  }


  getBaseUrl(): string {
    return `${this.url}`;
  }

  request<B = unknown>(method: string, url: string, body: B | null, p?: RequestOptions): Observable<HttpEvent<unknown>> {
    const options = p ? {...p} : {};
    const req = new HttpRequest(method, url, body, {
      params: this.getParams(options.params ? options.params : {}),
      reportProgress: options.reportProgress
    });
    return this.http.request(req);
  }

  private getParams(p?: HttpParamsLike): HttpParams {
    const params: HttpParamsLike = p ? {...p} : {};
    Object.assign(params, this.requestContext.params(p?.['store'] as string | undefined));
    let result = new HttpParams();
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== undefined) {
        result = result.append(key, value);
      }
    });
    return result;
  }
}
