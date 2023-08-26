export interface DomainReference {
  id?: number,
  domain: string,
  domainType?: DomainType;
  reference?: string,
}

export enum DomainType {
  APPLICATION = 'APPLICATION',
  SUB = 'SUB',
  CUSTOM = 'CUSTOM'
}
