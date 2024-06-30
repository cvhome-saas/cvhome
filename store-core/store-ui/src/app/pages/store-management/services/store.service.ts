import {Injectable} from '@angular/core';

import {CrudService} from '../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {StorageService} from '../../shared/services/storage.service';

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  constructor(
    private crudService: CrudService,
    private storageService: StorageService) {
  }


  getStore(code): Observable<any> {
    return this.crudService.get(`/store/api/v1/store/${code}?store=${code}`);
  }

  getAllocations(code): Observable<any> {
    return this.crudService.get(`/manager/api/v1/router/allocates?store=${code}`);
  }

  allocateDomain(store, domain): Observable<any> {
    return this.crudService.post(`/manager/api/v1/router/allocate?domain=${domain}&store=${store}`, {});
  }

  removeDomain(store, domain): Observable<any> {
    return this.crudService.delete(`/manager/api/v1/router/remove?domain=${domain}&store=${store}`);
  }

  getListOfStores(params): Observable<any> {
    return this.crudService.get(`/manager/api/v1/store-manager/private/store`, params);
  }

  getListOfMerchantStoreNames(params): Observable<any> {
    return this.crudService.get(`/v1/private/stores/names`, params);
  }

  checkIfStoreExist(name): Observable<any> {
    const params = {
      name
    };
    return this.crudService.get(`/manager/api/v1/store-manager/private/store/unique`, params);
  }

  createStore(store: any): Observable<any> {
    return this.crudService.post(`/manager/api/v1/store-manager/private/store`, store);
  }

  deleteStore(store: any): Observable<any> {
    return this.crudService.delete(`/store/api/v1/private/store/${store}?store=${store}`);
  }

  updateStore(store: any): Observable<any> {
    return this.crudService.put(`/store/api/v1/private/store/${store.code}?store=${store.code}`, store);
  }

  // PAGE CONTENT

  getPageContent(pageCode: string, store: string): Observable<any> {
    const params = {
      lang: '_all',
      store
    };
    return this.crudService.getWithEmpty(`/store/api/v1/private/content/any/${pageCode}?store=${store}`, params);
  }

  updatePageContent(store, id, content: any): Observable<any> {
    return this.crudService.put(`/store/api/v1/private/content/${id}?store=${store}`, content);
  }

  createPageContent(content: any, storeCode: string): Observable<any> {

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

  addStoreLogo(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store/api/v1/private/store/${store}/marketing/logo?store=${store}`, uploadData);
  }

  removeStoreLogo(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/logo?store=${store}`);
  }

  addStoreBanner(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store/api/v1/private/store/${store}/marketing/banner?store=${store}`, uploadData);
  }

  removeStoreBanner(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/banner?store=${store}`);
  }

  // end BRANDING

}
