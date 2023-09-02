import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class DomainReferenceService {

    constructor(private httpClient: HttpClient) {

    }

    save(domainReference: DomainReference): Observable<DomainReference> {
        return this.httpClient.post<DomainReference>("/domain/api/v1/domain-reference", domainReference)
    }

    getAllDomainReferencesOrders(): Observable<DomainReferenceOrder[]> {
        return this.httpClient.get<DomainReferenceOrder[]>("/domain/api/v1/domain-reference/get-all-domain-references")
    }

    orderHttps(id: number): Observable<DomainReferenceOrder> {
        return this.httpClient.post<DomainReferenceOrder>(`/domain/api/v1/domain-reference-acm/order?domainId=${id}`, null)
    }
}
export interface DomainReference {
  id?: number,
  domain: string,
  reference?: string,
  domainType?: DomainType;
  domainStatus?: DomainStatus;
  createdDate?: Date,
}

export enum DomainType {
  APPLICATION = 'APPLICATION',
  SUB = 'SUB',
  CUSTOM = 'CUSTOM'
}

export enum DomainStatus {
  INITIATED = 'INITIATED', ATTACHED = 'ATTACHED', UNATTACHED = 'UNATTACHED'
}

export interface DomainCertificateOrder {
  id: number;
  location: string
  domain: string
  certificateOrderStatus: CertificateOrderStatus
  challenges: Challenges;
}

export interface DomainReferenceOrder {
  domainReference: DomainReference,
  domainCertificateOrder?: DomainCertificateOrder
}

export enum CertificateOrderStatus {
  REQUESTED = 'REQUESTED',
  VALIDATED_VALID = 'VALIDATED_VALID',
  VALIDATED_INVALID = 'VALIDATED_INVALID',
  GENERATED = 'GENERATED'
}

export interface Challenges {
  challenges: Map<string, Map<string, string>>;
}
