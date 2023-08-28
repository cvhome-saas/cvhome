import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DomainReference, DomainReferenceOrder} from "../model/domain-reference";

@Injectable({
  providedIn: 'root'
})
export class DomainReferenceService {

  constructor(private httpClient: HttpClient) {

  }

  save(domainReference: DomainReference): Observable<DomainReference> {
    return this.httpClient.post<DomainReference>("/user/api/v1/domain-reference", domainReference)
  }

  getAllDomainReferencesOrders(): Observable<DomainReferenceOrder[]> {
    return this.httpClient.get<DomainReferenceOrder[]>("/user/api/v1/domain-reference/get-all-domain-references")
  }

  orderHttps(id: number): Observable<DomainReference> {
    return this.httpClient.post<DomainReference>(`/user/api/v1/domain-reference-acm/order?domainId=${id}`, null)
  }
}
