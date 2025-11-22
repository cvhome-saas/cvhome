import {Injectable} from "@angular/core";
import {CrudService} from "../../shared/services/crud.service";

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

  checkCodeBoxExist(code, params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/box/' + code + '/exists', params);
  }

  updateBox(id,  object: any, param: any) {
    return this.crudService.put('/store-pod-gateway/content/api/v1/private/content/box/' + id , object, param)

  }

  createBox( object: any) {
    return this.crudService.post('/store-pod-gateway/content/api/v1/private/content/box', object)
  }

  images() {
    return this.crudService.get('/store-pod-gateway/content/api/v1/content/images')
  }

  deleteImage( e) {
    return this.crudService.delete(`/store-pod-gateway/content/api/v1/private/content/?contentType=IMAGE&name=${e}`);
  }

  saveImage(formData: FormData) {
    return this.crudService.post(`/store-pod-gateway/content/api/v1/private/files`, formData)

  }

  checkCodePageExist(code: string) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/page/' + code + '/exists');
  }

  updatePage(id,  object: any) {
    return this.crudService.put('/store-pod-gateway/content/api/v1/private/content/page/' + id, object)

  }

  createPage( object: any) {
    return this.crudService.post('/store-pod-gateway/content/api/v1/private/content/page', object);
  }

  getPage(uniqueCode: string) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/pages/' + uniqueCode )

  }

  pages(params: any) {
    return this.crudService.get('/store-pod-gateway/content/api/v1/private/content/pages', params)
  }

  deleteContent(id) {
    return this.crudService.delete('/store-pod-gateway/content/api/v1/private/content/' + id + '?id=' + id)

  }
}
