import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../common/BaseTable';

@Injectable({
  providedIn: 'root'
})
export class CustomersService {
  private readonly crudService = inject(CrudService);


  getCustomers(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get('/spg/checkout/api/v1/private/customers', params);
  }

}
