import {Injectable} from "@angular/core";
import {CrudService} from "../../../shared/services/crud.service";

@Injectable({
  providedIn: 'root'
})
export class ContentService {

  constructor(private crudService: CrudService) {
  }


  getBoxes(params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/boxes', params);
  }

  getBox(uniqueCode: string, params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/boxes/' + uniqueCode, params);
  }

  checkCodeBoxExist(store: string, code, params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/box/' + code + '/exists?store=' + store, params);
  }

  updateBox(id, store: string, object: any, param: any) {
    return this.crudService.put('/store-pod-gateway/content/api/v1/private/content/box/' + id + '?store=' + store, object, param)

  }

  createBox(store: string, object: any) {
    return this.crudService.post('/store-pod-gateway/content/api/v1/private/content/box?store=' + store, object)
  }

  images(store: string) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/content/images?store=' + store)
  }

  deleteImage(store: string, e) {
    return this.crudService.delete(`/store-pod-gateway/content/api/v1/private/content/?store=${store}&contentType=IMAGE&name=${e}`);
  }

  saveImage(store: string, formData: FormData) {
    return this.crudService.post(`/store-pod-gateway/content/api/v1/private/files?store=${store}`, formData)

  }

  checkCodePageExist(code: string, store: string) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/page/' + code + '/exists?store=' + store);
  }

  updatePage(id, store: string, object: any) {
    return this.crudService.put('/store-pod-gateway/content/api/v1/private/content/page/' + id + "?store=" + store, object)

  }

  createPage(store: string, object: any) {
    return this.crudService.post('/store-pod-gateway/content/api/v1/private/content/page?store=' + store, object);
  }

  getPage(uniqueCode: string, store: string) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/content/pages/' + uniqueCode + '?lang=_all&store=' + store)

  }

  pages(params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/pages', params)
  }

  deleteContent(id, store: string) {
    return this.crudService.delete('/store-pod-gateway/content/api/v1/private/content/' + id + '?id=' + id + "&store=" + store)

  }
}
