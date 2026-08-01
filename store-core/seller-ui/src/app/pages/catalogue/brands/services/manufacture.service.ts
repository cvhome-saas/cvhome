import { Injectable, inject } from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {PageT} from '../../../shared/table/table.types';
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
