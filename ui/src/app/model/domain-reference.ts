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
