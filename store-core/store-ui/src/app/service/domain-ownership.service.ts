import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class DomainOwnershipService {
    private readonly DOMAIN_OWNERSHIP_BASE_URL: string = '/dcm/api/v1/domain-ownership';

    constructor(private httpClient: HttpClient) {
    }

    getProving(request: Domain): Observable<ProvingResponse> {
        return this.httpClient.post<ProvingResponse>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/get-proving`, request)
    }

    checkAvailability(request: Domain): Observable<AvailabilityResponse> {
        return this.httpClient.post<AvailabilityResponse>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/check-availability`, request)
    }

    defaultDomain(): Observable<Domain> {
        return this.httpClient.get<Domain>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/default-domain`)
    }

    register(request: RegisterDomainRequest): Observable<RegisterDomainResponse> {
        return this.httpClient.post<RegisterDomainResponse>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/register`, request)
    }

    getReference(request: Domain): Observable<DomainReferenceResponse> {
        return this.httpClient.post<DomainReferenceResponse>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/get-reference`, request)
    }

    changeReference(request: DomainChangeReferenceRequest): Observable<DomainChangeReferenceResponse> {
        return this.httpClient.post<DomainChangeReferenceResponse>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/change-reference`, request)
    }

    getRegisteredDomains(): Observable<RegisteredDomain[]> {
        return this.httpClient.get<RegisteredDomain[]>(`${this.DOMAIN_OWNERSHIP_BASE_URL}/get-registered-domains`)
    }


}

export interface Domain {
    domain: string
}

export interface ProvingResponse {
    record: string;
    value: string;
}

export interface AvailabilityResponse {
    available: boolean
}

export enum DomainType {
    APPLICATION_RESERVED = 'APPLICATION_RESERVED',
    SAS_SUB = 'SAS_SUB',
    SAS_CUSTOM = 'SAS_CUSTOM'

}

export enum ReferenceType {
    STORE = 'STORE'
}

export interface Reference {
    reference: string;
    referenceType: ReferenceType;
}

export interface RegisterDomainRequest {
    domain: Domain;
    domainType: DomainType;
    reference?: Reference;
    recommendedChallengeValidationType?: ChallengeValidationType;
}

export enum ChallengeValidationType {
    Http01 = 'Http01',
    TlsAlpn01 = 'TlsAlpn01',
    Dns01 = 'Dns01'
}

export interface RegisterDomainResponse {
    domain: Domain;
    reference: Reference;
}

export interface DomainReferenceResponse {
    reference: Reference;
}

export interface DomainChangeReferenceRequest {
    domain: Domain;
    reference: Reference;
}

export interface DomainChangeReferenceResponse {
    domain: Domain;
    reference: Reference;
}

export enum DomainCertificateStatus {
    INITIATED = 'INITIATED',
    FIRST_ORDERING = 'FIRST_ORDERING',
    RENEWING_ORDER_PROCESS = 'RENEWING_ORDER_PROCESS',
    EXPIRED_CERTIFICATE = 'EXPIRED_CERTIFICATE',
    EXPIRING_CERTIFICATE_SOON = 'EXPIRING_CERTIFICATE_SOON',
    ACTIVE_CERTIFICATE_GENERATED = 'ACTIVE_CERTIFICATE_GENERATED',
    FAILED_CERTIFICATE_GENERATING = 'FAILED_CERTIFICATE_GENERATING'

}

export interface RegisteredDomain {
    domain: Domain;
    domainType: DomainType;
    reference: Reference;
    certificateStatus: DomainCertificateStatus
}
