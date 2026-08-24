/**
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

/*
 * Everything below is the store-settings surface: the fields the console edits rather than merely
 * reads. `MerchantStore` above stayed narrow on purpose while only the invoice letterhead consumed
 * it; the settings page is the screen that owns the rest, so it is declared here now.
 */

/** Mirrors `commons/domain/SocialLink` (a record). The storefront footer's links. */
export interface SocialLink {
  readonly provider: string;
  readonly url: string;
}

/**
 * Mirrors `commons/domain/ReadableSliderImage` (a record) — and that is the whole of it.
 *
 * The design draws a scheduling tag, a click-through link and a `1600×640 · 248 KB · JPG` meta
 * line against each slide. None of those are stored: a slide is a position, a filename and a URL.
 * See lessons.md, "Store management — slider images carry no schedule, link or file metadata".
 */
export interface SliderImage {
  readonly priority: number;
  readonly name: string;
  readonly url?: string;
}

/** Mirrors `commons/domain/ManagerStoreDomain` (a record). No status field — see below. */
export interface ManagerStoreDomain {
  readonly domain: string;
  readonly domainType: string;
}

/** Mirrors tenancy `manager/controller/SaasController#saasProperties`. */
export interface SaasProperties {
  /** The tenant's alias on the platform's apex, which is what a custom domain must CNAME to. */
  readonly alis: string;
  readonly domain: string;
}

/**
 * The whole store as `GET /spg/merchant/api/v1/private/store` answers it.
 *
 * Extends the narrow `MerchantStore` with the rest of `ReadableMerchantStore`. Every field is
 * optional because the server declares none of them `@NotNull` on the read side and a store that
 * has not been filled in genuinely omits most — seller-core's copy typed them all as required
 * under `strictNullChecks: false`, which was simply untrue.
 */
export interface ReadableMerchantStore extends MerchantStore {
  readonly org?: string;
  readonly theme?: string;
  readonly colorTheme?: string;
  readonly template?: string;
  readonly defaultLanguage?: string;
  /** Plain language codes, not objects. */
  readonly supportedLanguages?: readonly string[];
  readonly currencyFormatNational?: boolean;
  readonly useCache?: boolean;
  readonly requireLoginForOrderPlacement?: boolean;
  /** `MeasureUnit` — centimetres or inches. */
  readonly dimension?: string;
  /** `WeightUnit` — kilograms or pounds. */
  readonly weight?: string;
  readonly banner?: StoreImage | null;
  readonly socialLinks?: readonly SocialLink[];
  readonly sliderImages?: readonly SliderImage[];
  readonly storeDomains?: readonly ManagerStoreDomain[];
}

/**
 * Mirrors `merchant-commons/model/merchant/PersistableMerchantStore`.
 *
 * Three endpoints take this same body and each reads a different slice of it: `PUT /private/store`
 * updates the store proper, `PUT /private/store/social-links` reads only `socialLinks`, and
 * `PUT /private/store/marketing/slider-images` reads only `sliderImages`. That is why the two
 * marketing saves still send a store-shaped object rather than a bare list.
 *
 * seller-core's copy carried an `[key: string]: unknown` index signature, which let any misspelled
 * field through the compiler untouched. Removed here — under `strict` the fields are the contract.
 */
export interface PersistableMerchantStore {
  readonly id: string;
  readonly name?: string;
  readonly email?: string;
  readonly phone?: string;
  readonly org?: string;
  readonly currency?: string;
  readonly currencyFormatNational?: boolean;
  readonly defaultLanguage?: string;
  readonly supportedLanguages?: readonly string[];
  readonly countryIsoCode?: string;
  readonly theme?: string;
  readonly colorTheme?: string;
  readonly template?: string;
  /** `LocalDate`: `YYYY-MM-DD`. */
  readonly inBusinessSince?: string;
  readonly useCache?: boolean;
  readonly requireLoginForOrderPlacement?: boolean;
  readonly dimension?: string;
  readonly weight?: string;
  readonly retailerStore?: string;
  readonly address?: StoreAddress;
  readonly socialLinks?: readonly SocialLink[];
  /** Only `priority` and `name` are read back off this — the URL is derived by the server. */
  readonly sliderImages?: readonly {readonly priority: number; readonly name: string}[];
  readonly storeDomains?: readonly ManagerStoreDomain[];
}
