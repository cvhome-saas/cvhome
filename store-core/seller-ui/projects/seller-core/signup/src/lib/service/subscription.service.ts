import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {CrudService, Table} from 'seller-core';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);


  table(): Observable<Table> {
    return this.crudService.get("control-plane/api/v1/subscription-plan/public/tables");
  }
}
