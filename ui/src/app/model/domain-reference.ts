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
