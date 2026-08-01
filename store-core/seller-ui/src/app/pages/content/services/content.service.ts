import { Injectable, inject } from "@angular/core";
import {Observable} from "rxjs";
import {CrudService} from "../../shared/services/crud.service";
import {PageT, StorePageRequest} from "../../shared/table/table.types";

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private readonly crudService = inject(CrudService);


  getBoxes(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/boxes', params);
  }

  getBox(uniqueCode: string, params: any) {
    return this.crudService.get('/spg/merchant/api/v1/private/content/boxes/' + uniqueCode, params);
  }

  checkCodeBoxExist(code: string, params: any) {
    return this.crudService.get('/spg/merchant/api/v1/private/content/box/' + code + '/exists', params);
  }

  updateBox(id: number | string, object: any, param: any) {
    return this.crudService.put('/spg/merchant/api/v1/private/content/box/' + id, object, param)

  }

  createBox(object: any) {
    return this.crudService.post('/spg/merchant/api/v1/private/content/box', object)
  }

  images() {
    return this.crudService.get('/spg/merchant/api/v1/content/images')
  }

  deleteImage(name: string) {
    return this.crudService.delete(`/spg/merchant/api/v1/private/content/?contentType=IMAGE&name=${name}`);
  }

  saveImage(formData: FormData) {
    return this.crudService.post(`/spg/merchant/api/v1/private/files`, formData)

  }

  checkCodePageExist(code: string) {
    return this.crudService.get('/spg/merchant/api/v1/private/content/page/' + code + '/exists');
  }

  updatePage(id: number | string, object: any) {
    return this.crudService.put('/spg/merchant/api/v1/private/content/page/' + id, object)

  }

  createPage(object: any) {
    return this.crudService.post('/spg/merchant/api/v1/private/content/page', object);
  }

  getPage(uniqueCode: string) {
    return this.crudService.get('/spg/merchant/api/v1/private/content/pages/' + uniqueCode)

  }

  pages(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/pages', params)
  }

  deleteContent(id: number | string) {
    return this.crudService.delete('/spg/merchant/api/v1/private/content/' + id + '?id=' + id)

  }
}
