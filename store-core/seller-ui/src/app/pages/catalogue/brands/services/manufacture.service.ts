import {Injectable} from '@angular/core';

import {CrudService} from '../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ManufactureService {

  constructor(
    private readonly crudService: CrudService,
  ) {
  }

  getManufacturers(): Observable<any> {
    return this.crudService.get(`/spg/catalog/api/v1/manufacturers`);
  }
}
