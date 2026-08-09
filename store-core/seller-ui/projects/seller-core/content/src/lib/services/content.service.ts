import { Injectable, inject } from "@angular/core";
import {Observable} from "rxjs";
import {CrudService, HttpParamsLike} from "seller-core";
import {PageT, StorePageRequest} from "seller-core";
import {EntityExists} from "seller-core";
import {
  ContentFolder,
  CreatedContentEntity,
  PersistableContentBox,
  PersistableContentPage,
  ReadableContentBox,
  ReadableContentPage
} from "../models/content.model";

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private readonly crudService = inject(CrudService);


  getBoxes(params: StorePageRequest): Observable<PageT<ReadableContentBox>> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/boxes', params);
  }

  getBox(uniqueCode: string, params: HttpParamsLike): Observable<ReadableContentBox> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/boxes/' + uniqueCode, params);
  }

  checkCodeBoxExist(code: string, params: HttpParamsLike): Observable<EntityExists> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/box/' + code + '/exists', params);
  }

  updateBox(id: number | string, object: PersistableContentBox, param: HttpParamsLike): Observable<void> {
    return this.crudService.put('/spg/merchant/api/v1/private/content/box/' + id, object, param)

  }

  createBox(object: PersistableContentBox): Observable<CreatedContentEntity> {
    return this.crudService.post('/spg/merchant/api/v1/private/content/box', object)
  }

  images(): Observable<ContentFolder> {
    return this.crudService.get('/spg/merchant/api/v1/content/images')
  }

  deleteImage(name: string): Observable<void> {
    return this.crudService.delete(`/spg/merchant/api/v1/private/content/?contentType=IMAGE&name=${name}`);
  }

  saveImage(formData: FormData): Observable<void> {
    return this.crudService.post(`/spg/merchant/api/v1/private/files`, formData)

  }

  checkCodePageExist(code: string): Observable<EntityExists> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/page/' + code + '/exists');
  }

  updatePage(id: number | string, object: PersistableContentPage): Observable<void> {
    return this.crudService.put('/spg/merchant/api/v1/private/content/page/' + id, object)

  }

  createPage(object: PersistableContentPage): Observable<CreatedContentEntity> {
    return this.crudService.post('/spg/merchant/api/v1/private/content/page', object);
  }

  getPage(uniqueCode: string): Observable<ReadableContentPage> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/pages/' + uniqueCode)

  }

  pages(params: StorePageRequest): Observable<PageT<ReadableContentPage>> {
    return this.crudService.get('/spg/merchant/api/v1/private/content/pages', params)
  }

  deleteContent(id: number | string): Observable<void> {
    return this.crudService.delete('/spg/merchant/api/v1/private/content/' + id + '?id=' + id)

  }
}
