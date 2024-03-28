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
    id: ManagerStoreId;
    name: string;
    owner: IdentityId;
}

export interface Page<T> {
    content?: T[];
}

export enum Country {
    EG = 'EG',
    KSA = 'KSA',
    UAE = 'UAE'
}

export interface Email {
    email: string;
}

export interface User {
    id: string;
    username: string;
    email: string;
    firstname: string;
    enabled: boolean,
    groups: Group[]
}

export enum Group {
    ORG_ADMIN = 'ORG_ADMIN', STORE_ADMIN = 'STORE_ADMIN', STORE_MODERATOR = 'STORE_MODERATOR', CUSTOMER = 'CUSTOMER'
}

export interface ResetPassword {
    password: string
}