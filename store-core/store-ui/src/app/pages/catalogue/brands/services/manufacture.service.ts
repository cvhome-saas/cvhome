import {Injectable} from '@angular/core';

import {CrudService} from '../../../../shared/services/crud.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ManufactureService {

  constructor(
    private crudService: CrudService,
  ) {
  }

  getManufacturers(store): Observable<any> {
    return this.crudService.get(`/store-pod-gateway/catalog/api/v1/manufacturers?store=${store}`);
  }
}
