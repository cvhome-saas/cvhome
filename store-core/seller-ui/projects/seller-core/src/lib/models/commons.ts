export interface ManagerStoreId {
  id: string;
}

export interface IdentityId {
  id: string;
}

export interface CreateStoreRequest {
  name?: string;
  country?: Country;
  email?: Email;
}

/** Mirrors tenancy manager/commons/dto/ManagerStoreDto (record) */
export interface ManagerStore {
  id: ManagerStoreId
  name: string
  orgId: IdentityId
  podId: PodId
  provisioningState: string
}

export interface Page<T> {
  content?: T[];
}

export enum Country {
  EG = 'EG',
  SA = 'SA',
  UAE = 'UAE'
}

export interface Email {
  email: string;
}

export interface PodId {
  id: string
}
