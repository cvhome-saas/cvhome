import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../shared/table/table.types';
import {ReadableCustomer} from '../../orders/models/order.model';

@Injectable({
  providedIn: 'root'
})
export class CustomersService {
  private readonly crudService = inject(CrudService);


  getCustomers(params: StorePageRequest): Observable<PageT<ReadableCustomer>> {
    return this.crudService.get('/spg/checkout/api/v1/private/customers', params);
  }

}
