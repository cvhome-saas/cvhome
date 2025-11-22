import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';

@Injectable({
  providedIn: 'root'
})
export class CustomersService {

  constructor(
    private crudService: CrudService
  ) {
  }

  getCustomers(params): Observable<any> {
    return this.crudService.get('/store-pod-gateway/order/api/v1/private/customers', params);
  }

}
