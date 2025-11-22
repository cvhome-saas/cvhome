import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {

  constructor(private httpClient: HttpClient) {
  }

  table(): Observable<Table> {
    return this.httpClient.get<Table>("control-plane/api/v1/subscription-plan/public/tables");
  }

  details(): Observable<SubscriptionDetails> {
    return this.httpClient.get<SubscriptionDetails>("control-plane/api/v1/subscription/subscription-plan-details");
  }
}
export interface SubscriptionDetails {
  createdDate: string
  lastRenewedDate: string
  endDate: string
  deActivatedDate: any
  subscriptionPlan: string
  recurringPlan: string
  status: string
}


export interface Table {
  tables: Tables
  freeOption: FreeOption
}

export interface Tables {
  MONTH: Month
  YEAR: Year
}

export interface Month {
  options: Option[]
}

export interface Option {
  id: Id
  productId: ProductId
  cost: Cost
  previousCost: Cost
  subscriptionPlan: string
  limits: Limits
  feature: FeatureList
  recurringPlan: string
}

export interface Id {
  id: string
}

export interface ProductId {
  productId: string
}

export interface Cost {
  currency: string
  price: number
}

export interface Limits {
  limits: Limit[]
}

export interface Limit {
  limitKey: string
  limit: number
}

export interface FeatureList {
  features: Feature[]
}

export interface Feature{
  code: string
}

export interface Year {
  options: Option[]
}

export interface FreeOption {
  cost: Cost
  subscriptionPlan: string
  limits: Limits
  feature: FeatureList
}
