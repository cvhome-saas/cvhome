import { Injectable, inject } from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from 'seller-core';
import {PageT, StorePageRequest} from 'seller-core';
import {ReadableCustomer} from 'seller-core/orders';

@Injectable({
  providedIn: 'root'
})
export class CustomersService {
  private readonly crudService = inject(CrudService);


  getCustomers(params: StorePageRequest): Observable<PageT<ReadableCustomer>> {
    return this.crudService.get('/spg/checkout/api/v1/private/customers', params);
  }

}
