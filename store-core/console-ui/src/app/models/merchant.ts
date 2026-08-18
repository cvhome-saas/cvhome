/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/models/store.ts, narrowed and corrected
 * against `merchant-commons/model/merchant/ReadableMerchantStore.java` and its parents.
 *
 * The merchant store as the pod describes it — the seller's own identity: what it is called, where
 * it trades from, how it is reached. Distinct from `@models/tenancy`'s `ManagerStore`, which is
 * tenancy's row about the store (its pod, its provisioning state, its billing status) and carries
 * none of this.
 *
 * Only the fields the console reads today are declared. seller-core's copy declares the whole
 * aggregate — themes, slider images, social links, supported languages, the parent store — under
 * `strictNullChecks: false`, which stated that all of them are always present. They are not:
 * `logo`, `banner` and `address` are frequently null on a store that has not been filled in, and
 * `inBusinessSince` is a `LocalDate`, so it serializes as `YYYY-MM-DD` rather than an instant.
 * The rest is left to the store-settings module, which is the screen that actually edits it.
 */

/** Mirrors `store-core/model/content/ReadableImage` — a name and a resolvable path. */
export interface StoreImage {
  readonly name?: string;
  readonly path?: string;
}

/** Mirrors `store/model/references/ReadableBaseAddress` → `BaseAddress`. */
export interface StoreAddress {
  readonly address?: string;
  readonly city?: string;
  readonly postalCode?: string;
  /** ISO country code, serialized from `CountryIsoCode`. */
  readonly country?: string;
  /** Zone code, serialized from `ZoneCode`. */
  readonly stateProvince?: string;
  readonly active?: boolean;
}

/**
 * Mirrors `ReadableMerchantStore` → `MerchantStoreDetails`, as returned by
 * `GET store-manager/private/store/{code}` (tenancy proxies the pod's answer and adds `pod`).
 */
export interface MerchantStore {
  readonly id: string;
  readonly name: string;
  readonly email?: string;
  readonly phone?: string;
  readonly currency?: string;
  readonly countryIsoCode?: string;
  /** `LocalDate` on the server: `YYYY-MM-DD`, not an instant. */
  readonly inBusinessSince?: string;
  readonly address?: StoreAddress;
  readonly logo?: StoreImage | null;
  readonly pod?: {readonly id: string};
}
