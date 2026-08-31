/**
 * Public signup: creates an organization and its first administrator in one call
 * (`tenancy` → `SignUpApi#create` → `SignupServiceImpl#createOrgUser`).
 *
 * Two corrections against the seller-core original, both verified against the Java:
 *
 * - The request wrapper is `CreateOrgRequest(SignUpUser user)` — a record with **one** component. seller-core's
 *   `SignUpForm` also carried a `subscriptionPlan`, which no server field ever read. Sending it was harmless and
 *   meaningless; declaring it here would suggest the plan chosen on the pricing page reaches billing, and it does not.
 * - The response is `ReadableUser`, not `{status: string}`. Nothing rendered the old shape, so nothing depended on
 *   the mistake.
 */

/**
 * Mirrors tenancy's `SignUpUser` — the request type public signup actually takes.
 *
 * It used to be uaa's `PersistableUser`, which is the type the *platform* writes a user with: `roles`, `org`,
 * `store`, `active` and `id` were all on the wire and settable by anyone. tenancy now takes a record that
 * cannot express them, which is why this interface has no such fields to omit.
 *
 * `organizationName` exists on the server and is deliberately not sent: an omitted name is defaulted to the
 * administrator's own, and a field with a good default is friction on the one screen that cannot afford any.
 * See lessons.md, "Auth — signup collects no organization name".
 */
export interface PersistableUser {
  readonly firstName: string;
  readonly lastName: string;
  readonly emailAddress: string;
  readonly password: string;
  /**
   * Compared with `password` by tenancy's `@PasswordsMatch`, which reports the mismatch on this field.
   *
   * It used to be read by **nothing**: a payload with `password: "a"` and `repeatPassword: "b"` was accepted
   * with 200 and the account created with the first of the two. The form was the only place they were ever
   * compared.
   */
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
