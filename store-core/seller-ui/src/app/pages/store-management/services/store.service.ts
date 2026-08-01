import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {Pod} from "./pod.service";
import {EntityExists} from '../../shared/models/entity.model';
import {ManagerStore} from '../../shared/models/commons';
import {ManagerStoreDomain, SliderImage} from '../models/store';
import {StorePageRequest} from '../../shared/table/table.types';
import {
  LandingPageContent,
  PersistableMerchantStore,
  PersistablePaymentConfiguration,
  PersistableSocialLoginConfig,
  ReadableMerchantStoreWithPod,
  ReadablePaymentConfiguration,
  ReadableSocialLoginConfig,
  SaasProperties,
  SocialLink,
  SpringPage
} from '../models/store-service.model';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private crudService = inject(CrudService);



  getStore(store: string): Observable<ReadableMerchantStoreWithPod> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store/${store}`);
  }

  getAllocations(store: string): Observable<ManagerStoreDomain[]> {
    return this.crudService.get(`/spg/merchant/api/v1/router/private/allocates`, {store});
  }

  storePodByStoreId(store: string): Observable<Pod> {
    return this.crudService.get(`/control-plane/api/v1/router/store-pod-by-store-id`, {store});
  }

  saasProperties(): Observable<SaasProperties> {
    return this.crudService.get(`/control-plane/api/v1/saas/public/saas-properties`);
  }

  allocateDomain(store: string, domain: string): Observable<void> {
    return this.crudService.post(`/spg/merchant/api/v1/router/private/allocate?domain=${domain}`, {store});
  }

  removeDomain(domain: string, store: string): Observable<void> {
    return this.crudService.delete(`/spg/merchant/api/v1/router/private/remove?domain=${domain}`, {store});
  }

  getListOfStores(params: StorePageRequest): Observable<SpringPage<ManagerStore>> {
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store`, params);
  }

  checkIfStoreExist(name: string): Observable<EntityExists> {
    const params = {
      name
    };
    return this.crudService.get(`/control-plane/api/v1/store-manager/private/store/unique`, params);
  }

  createStore(store: Record<string, unknown>): Observable<ManagerStore> {
    return this.crudService.post(`/control-plane/api/v1/store-manager/private/store`, store);
  }

  deleteStore(store: string): Observable<void> {
    return this.crudService.delete(`/spg/merchant/api/v1/private/store`, {store});
  }

  updateStore(store: PersistableMerchantStore): Observable<void> {
    return this.crudService.put(`/spg/merchant/api/v1/private/store`, store);
  }

  // PAGE CONTENT

  getPageContent(store: string, pageCode: string): Observable<LandingPageContent> {
    return this.crudService.get(`/spg/merchant/api/v1/private/content/any/${pageCode}`, {store});
  }

  updatePageContent(store: string, code: string | number, content: Record<string, unknown>): Observable<void> {
    return this.crudService.put(`/spg/merchant/api/v1/private/content/${code}`, content, {store});
  }

  createPageContent(content: Record<string, unknown>, store: string): Observable<void> {

    return this.crudService.post(`/spg/merchant/api/v1/private/content`, content, {store});
  }

  updateSocialNetworks(store: string, body: Record<string, unknown>): Observable<void> {
    return this.crudService.post(`/v1/private/store/${store}/marketing`, body);
  }

  addStoreLogo(store: string, file: File): Observable<void> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/logo`, uploadData, {store});
  }

  removeStoreLogo(store: string): Observable<void> {
    return this.crudService.delete(`/v1/private/store/${store}/marketing/logo`);
  }

  addStoreBanner(store: string, file: File): Observable<void> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/banner`, uploadData, {store});
  }

  addStoreSliderImage(store: string, file: File): Observable<SliderImage> {
    const uploadData = new FormData();
    uploadData.append('file', file, file.name);
    return this.crudService.post(`/spg/merchant/api/v1/private/store/marketing/add-slider-image`, uploadData, {store});
  }

  saveStoreImageSliders(store: string, sliderImages: SliderImage[]): Observable<void> {
    return this.crudService.put(`/spg/merchant/api/v1/private/store/marketing/slider-images`, {
      sliderImages
    }, {store});
  }

  removeStoreBanner(store: string): Observable<void> {
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

  updateStoreSocialLinks(storeId: string, request: {socialLinks: SocialLink[]} & Record<string, unknown>): Observable<void> {
    return this.crudService.put(`/spg/merchant/api/v1/private/store/social-links`, request);
  }

  getSupportedSocialLoginProviders(): Observable<string[]> {
    return this.crudService.get(`/spg/cua/api/v1/private/social-login-config/supported-social-providers`);
  }

  getSocialLoginConfigs(store: string): Observable<ReadableSocialLoginConfig[]> {
    return this.crudService.get(`/spg/cua/api/v1/private/social-login-config`, {store});
  }

  updateSocialLoginConfigs(store: string, configs: PersistableSocialLoginConfig[]): Observable<void> {
    return this.crudService.post(`/spg/cua/api/v1/private/social-login-config`, configs, {store});
  }

  getSupportedPaymentTypes(): Observable<string[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration/supported-payment-types`);
  }

  getPaymentConfigs(store: string): Observable<ReadablePaymentConfiguration[]> {
    return this.crudService.get(`/spg/payment/api/v1/private/payment-configuration`, {store});
  }

  savePaymentConfig(store: string, config: PersistablePaymentConfiguration): Observable<void> {
    return this.crudService.post(`/spg/payment/api/v1/private/payment-configuration`, config, {store});
  }

  updatePaymentConfig(store: string, paymentType: string, config: PersistablePaymentConfiguration): Observable<void> {
    return this.crudService.put(`/spg/payment/api/v1/private/payment-configuration/${paymentType}`, config, {store});
  }

  deletePaymentConfig(store: string, paymentType: string): Observable<void> {
    return this.crudService.delete(`/spg/payment/api/v1/private/payment-configuration/${paymentType}`, {store});
  }
}
