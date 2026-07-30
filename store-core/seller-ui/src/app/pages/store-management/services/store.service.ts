import {Injectable} from '@angular/core';

import {CrudService} from '../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {SliderImage} from "../store-slider-images/store-slider-images.component";
import {Pod} from "./pod.service";

@Injectable({
  providedIn: 'root'
})
export class StoreService {

  constructor(
    private crudService: CrudService) {
  }


  getStore(store): Observable<any> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store/${store}`);
  }

  getAllocations(store: string): Observable<any> {
    return this.crudService.get(`/spg/merchant/api/v1/router/private/allocates`, {store});
  }

  storePodByStoreId(store: string): Observable<Pod> {
    return this.crudService.get(`/control-plane/api/v1/router/store-pod-by-store-id`, {store});
  }

  saasProperties(): Observable<any> {
    return this.crudService.get(`/control-plane/api/v1/saas/public/saas-properties`);
  }

  allocateDomain(store: string, domain: string): Observable<any> {
    return this.crudService.post(`/spg/merchant/api/v1/router/private/allocate?domain=${domain}`, {store});
  }

  removeDomain(domain: string, store: string): Observable<any> {
    return this.crudService.delete(`/spg/merchant/api/v1/router/private/remove?domain=${domain}`, {store});
  }

  getListOfStores(params): Observable<any> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store`, params);
  }

  checkIfStoreExist(name): Observable<any> {
    const params = {
      name
    };
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store/unique`, params);
  }

  createStore(store: any): Observable<any> {
    return this.crudService.post(`/control-plane/api/v1/store-manager/private/store`, store);
  }

  deleteStore(store: string): Observable<any> {
    return this.crudService.delete(`/spg/merchant/api/v1/private/store`, {store});
  }

  updateStore(store: any): Observable<any> {
    return this.crudService.put(`/spg/merchant/api/v1/private/store`, store);
  }

  // PAGE CONTENT

  getPageContent(store: string, pageCode: string): Observable<any> {
    return this.crudService.get(`/spg/merchant/api/v1/private/content/any/${pageCode}`, {store});
  }

  updatePageContent(store: string, code: string, content: any): Observable<any> {
    return this.crudService.put(`/spg/merchant/api/v1/private/content/${code}`, content, {store});
  }

  createPageContent(content: any, store: string): Observable<any> {

    return this.crudService.post(`/spg/merchant/api/v1/private/content`, content, {store});
  }

  updateSocialNetworks(store: string, body: any): Observable<any> {
    return this.crudService.post(`/v1/private/store/${store}/marketing`, body);
  }

  addStoreLogo(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/logo`, uploadData, {store});
  }

  removeStoreLogo(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/logo`);
  }

  addStoreBanner(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/banner`, uploadData, {store});
  }

  addStoreSliderImage(store: string, file: any): Observable<any> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/add-slider-image`, uploadData, {store});
  }

  saveStoreImageSliders(store: string, sliderImages: SliderImage[]) {
    return this.crudService.put(`/spg/merchant/api/v1/private/store/marketing/slider-images`, {
      sliderImages
    }, {store});
  }

  removeStoreBanner(store: string): Observable<any> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/banner`);
  }

  getSupportedThemes(): Observable<string[]> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/public/themes`);
  }

  getSupportedColorThemes(): Observable<string[]> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/public/color-themes`);
  }

  getSupportedSocialLinkProviders(): Observable<string[]> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/public/social-links-providers`);
  }

  updateStoreSocialLinks(storeId, request) {
    return this.crudService.put(`/spg/merchant/api/v1/private/store/social-links`, request);
  }

  getSupportedSocialLoginProviders(): Observable<string[]> {
    return this.crudService.get(`/spg/cua/api/v1/private/social-login-config/supported-social-providers`);
  }

  getSocialLoginConfigs(store: string): Observable<any> {
    return this.crudService.get(`/spg/cua/api/v1/private/social-login-config`, {store});
  }

  updateSocialLoginConfigs(store: string, configs: any[]): Observable<any> {
    return this.crudService.post(`/spg/cua/api/v1/private/social-login-config`, configs, {store});
  }

  getSupportedPaymentTypes(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-types`);
  }

  getPaymentConfigs(store: string): Observable<any> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration`, {store});
  }

  savePaymentConfig(store: string, config: any): Observable<any> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment-configuration`, config, {store});
  }

  updatePaymentConfig(store: string, paymentType: string, config: any): Observable<any> {
    return this.crudService.put(`/spg/payment/api/v1/private/payment-configuration/${paymentType}`, config, {store});
  }

  deletePaymentConfig(store: string, paymentType: string): Observable<any> {
    return this.crudService.delete(`/spg/payment/api/v1/private/payment-configuration/${paymentType}`, {store});
  }
}
