import { Injectable } from '@angular/core';

import { CrudService } from '../../shared/services/crud.service';
import { Observable } from 'rxjs';
import { StorageService } from '../../shared/services/storage.service';

@Injectable({
  providedIn: 'root'
})
export class StoreService {


  constructor(
    private crudService: CrudService,
    private storageService: StorageService
  ) {
  }


  getStore(code): Observable<any> {
    return this.crudService.get(`/store/api/v1/store/${code}?store=${code}`);
  }

  getListOfStores(params): Observable<any> {
    return this.crudService.get(`/manager/api/v1/store-manager/private/store`, params);
  }

  getListOfMerchantStoreNames(params): Observable<any> {
    return this.crudService.get(`/v1/private/stores/names`, params);
  }

  checkIfStoreExist(code): Observable<any> {
    const params = {
      'code': code,
    };
    return this.crudService.get(`/v1/private/store/unique`, params);
  }

  createStore(store: any): Observable<any> {
    return this.crudService.post(`/v1/private/store`, store);
  }

  deleteStore(storeCode: any): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${ storeCode }`);
  }

  updateStore(store: any): Observable<any> {
    return this.crudService.put(`/v1/private/store/${ store.code }`, store);
  }

  // PAGE CONTENT

  getPageContent(pageCode: string, store: string): Observable<any> {
    const params = {
      lang: '_all',
      store
    };
    return this.crudService.getWithEmpty(`/store/api/v1/private/content/any/${pageCode}?store=${store}`, params);
  }

  updatePageContent(store,id, content: any): Observable<any> {
    return this.crudService.put(`/store/api/v1/private/content/${id}?store=${store}`, content);
  }

  createPageContent(content: any, storeCode: string) : Observable<any> {

    return this.crudService.postWithStoreParam(`/store/api/v1/private/content`, content, storeCode);
  }

  // end PAGE CONTENT

  // start BRANDING

  getBrandingDetails(code): Observable<any> {
    return this.crudService.get(`/v1/private/store/${code}/marketing`);
  }

  updateSocialNetworks(body: any): Observable<any> {
    const code = this.storageService.getMerchant();
    return this.crudService.post(`/v1/private/store/${code}/marketing`, body);
  }

  addStoreLogo(store:string,file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store/api/v1/private/store/${store}/marketing/logo?store=${store}`, uploadData);
  }

  removeStoreLogo(code: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${code}/marketing/logo`);
  }

  // end BRANDING

}
