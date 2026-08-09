import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {CrudService} from "seller-core";
import {Table} from 'seller-core';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);


  table(): Observable<Table> {
    return this.crudService.get("control-plane/api/v1/subscription-plan/public/tables");
  }

  details(): Observable<SubscriptionDetails> {
    return this.crudService.get("control-plane/api/v1/subscription/subscription-plan-details");
  }
}

export interface SubscriptionDetails {
  createdDate: string
  lastRenewedDate: string
  endDate: string
  deActivatedDate: string | null
  subscriptionPlan: string
  recurringPlan: string
  status: string
}
