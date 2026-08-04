import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {CrudService} from "../../pages/shared/services/crud.service";
import {Table} from '../../pages/shared/models/subscription.model';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);


  table(): Observable<Table> {
    return this.crudService.get("control-plane/api/v1/subscription-plan/public/tables");
  }
}
