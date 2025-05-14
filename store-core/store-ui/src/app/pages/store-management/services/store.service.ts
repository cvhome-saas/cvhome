import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {SliderImage} from "../store-slider-images/store-slider-images.component";

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  constructor(
    private crudService: CrudService) {
  }


  getStore(store): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/merchant/api/v1/store/${store}?store=${store}`);
  }

  getAllocations(store): Observable<any> {
    return this.crudService.get(`/manager/api/v1/router/allocates?store=${store}`);
  }

  storePodByStoreId(store): Observable<any> {
    return this.crudService.get(`/manager/api/v1/router/store-pod-by-store-id?store=${store}`);
  }

  saasProperties(): Observable<any> {
    return this.crudService.get(`/manager/api/v1/saas/public/saas-properties`);
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
    return this.crudService.delete(`/store-pod-gateway/merchant/api/v1/private/store?store=${store}`);
  }

  updateStore(store: any): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/merchant/api/v1/private/store?store=${store.id}`, store);
  }

  // PAGE CONTENT

  getPageContent(pageCode: string, store: string): Observable<any> {
    const params = {
      store
    };
    return this.crudService.getWithEmpty(`/store-pod-gateway/content/api/v1/private/content/any/${pageCode}`, params);
  }

  updatePageContent(store, id, content: any): Observable<any> {
    return this.crudService.put(`/store-pod-gateway/content/api/v1/private/content/${id}?store=${store}`, content);
  }

  createPageContent(content: any, store: string): Observable<any> {

    return this.crudService.postWithStoreParam(`/store-pod-gateway/content/api/v1/private/content`, content, store);
  }

  updateSocialNetworks(store: string, body: any): Observable<any> {
    return this.crudService.post(`/v1/private/store/${store}/marketing`, body);
  }

  addStoreLogo(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store-pod-gateway/merchant/api/v1/private/store/marketing/logo?store=${store}`, uploadData);
  }

  removeStoreLogo(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/logo?store=${store}`);
  }

  addStoreBanner(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store-pod-gateway/merchant/api/v1/private/store/marketing/banner?store=${store}`, uploadData);
  }

  addStoreSliderImage(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/store-pod-gateway/merchant/api/v1/private/store/marketing/add-slider-image?store=${store}`, uploadData);
  }

  saveStoreImageSliders(store, sliderImages: SliderImage[]) {
    return this.crudService.put(`/store-pod-gateway/merchant/api/v1/private/store/marketing/slider-images?store=${store}`, {
      sliderImages
    });
  }

  removeStoreBanner(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/banner?store=${store}`);
  }

  getSupportedThemes(): Observable<string[]> {
    return this.crudService.get(`/manager/api/v1/store-manager/public/themes`);
  }

  getSupportedColorThemes(): Observable<string[]> {
    return this.crudService.get(`/manager/api/v1/store-manager/public/color-themes`);
  }

  getSupportedSocialLinkProviders(): Observable<string[]> {
    return this.crudService.get(`/manager/api/v1/store-manager/public/social-links-providers`);
  }

  updateStoreSocialLinks(storeId, request) {
    return this.crudService.put(`/store-pod-gateway/merchant/api/v1/private/store/social-links?store=${storeId}`, request);
  }
}
