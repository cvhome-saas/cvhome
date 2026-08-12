export interface IdentityId {
  id: string;
}

export interface CreateStoreRequest {
  name?: string;
  country?: Country;
  email?: Email;
}

/**
 * Mirrors tenancy manager/commons/dto/ManagerStoreDto (record).
 *
 * `id` is a bare string: a store id serializes as `"65f0…"`, unlike `orgId` and `podId`, which are still
 * `{id: "…"}` objects.
 */
export interface ManagerStore {
  id: string
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
