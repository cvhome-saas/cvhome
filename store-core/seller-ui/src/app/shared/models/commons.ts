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

export interface Store {
  id?: ManagerStoreId;
  name?: string;
  owner?: IdentityId;
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
