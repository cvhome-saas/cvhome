import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {CrudService} from '../../shared/services/crud.service';
import {PageT, StorePageRequest} from '../../common/BaseTable';

@Injectable({
  providedIn: 'root'
})
export class CustomersService {

  constructor(
    private readonly crudService: CrudService
  ) {
  }

  getCustomers(params: StorePageRequest): Observable<PageT<any>> {
    return this.crudService.get('/spg/checkout/api/v1/private/customers', params);
  }

}
