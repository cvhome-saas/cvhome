import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {CrudService} from "../../pages/shared/services/crud.service";

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly crudService = inject(CrudService);


  table(): Observable<Table> {
    return this.crudService.get("control-plane/api/v1/subscription-plan/public/tables");
  }
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

export interface Feature {
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
