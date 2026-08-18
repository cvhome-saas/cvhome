/**
 * Ported from seller-ui/projects/seller-core/signup/src/lib/domain/types.ts, corrected against the server.
 *
 * Public signup: creates an organization and its first administrator in one call
 * (`tenancy` → `SignUpApi#create` → `SignupServiceImpl#createOrgUser`).
 *
 * Two corrections against the seller-core original, both verified against the Java:
 *
 * - The request wrapper is `CreateOrgRequest(PersistableUser user)` — a record with **one** component. seller-core's
 *   `SignUpForm` also carried a `subscriptionPlan`, which no server field ever read. Sending it was harmless and
 *   meaningless; declaring it here would suggest the plan chosen on the pricing page reaches billing, and it does not.
 * - The response is `ReadableUser`, not `{status: string}`. Nothing rendered the old shape, so nothing depended on
 *   the mistake.
 */

/** Mirrors uaa-client's `PersistableUser` (the subset public signup fills in; the server sets the rest). */
export interface PersistableUser {
  readonly firstName: string;
  readonly lastName: string;
  readonly emailAddress: string;
  readonly password: string;
  /** Checked server-side as well as in the form — uaa rejects a mismatch rather than trusting the client. */
  readonly repeatPassword: string;
}

/** Mirrors tenancy's `CreateOrgRequest` record. */
export interface CreateOrgRequest {
  readonly user: PersistableUser;
}

/** Mirrors uaa-client's `ReadableUser`. `org` is the organization signup just created. */
export interface ReadableUser {
  readonly id: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly emailAddress: string;
  readonly userName: string;
  readonly org: string;
  readonly store: string | null;
  readonly active: boolean;
  readonly roles: readonly string[];
}
