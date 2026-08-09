import { Injectable, inject } from '@angular/core';

import {CrudService} from 'seller-core';
import {Observable} from 'rxjs';
import {PageT} from 'seller-core';
import {ReadableManufacturer} from '../models/brand.model';

@Injectable({
  providedIn: 'root'
})
export class ManufactureService {
  private readonly crudService = inject(CrudService);


  getManufacturers(): Observable<PageT<ReadableManufacturer>> {
    return this.crudService.get(`/spg/catalog/api/v1/manufacturers`);
  }
}
