import type {ConsoleLocale, IconName, Tone} from '@cvhome-saas/ui-kit';

/**
 * The store's settings surface.
 *
 * Shaped after the backend contracts it will eventually be served from —
 * `ReadableMerchantStore` + `MerchantStoreDetails`, `ManagerStoreDomain`,
 * `ReadableSocialLoginConfig` and `ReadablePaymentConfiguration` — so swapping the mock for HTTP is a
 * rename rather than a rewrite. Provider lists are the real enums, not the mockup's inventions.
 */

/**
 * Store *configuration*. Appearance — branding, the home page, the slider and social links — moved to the
 * content service, which owns the media library those images come from, and is edited in the content hub.
 */
export type SettingsSectionKey =
  | 'domain'
  | 'details'
  | 'payments';

/** `DomainType` in `commons`. A store always has the first and may add the second. */
export type DomainType = 'SUB_DOMAIN' | 'CUSTOM_DOMAIN';

/**
 * What a DNS lookup found.
 *
 * There is no `unverified` member, and its absence is the design: an allocated domain is not in an
 * unchecked state, because the field refuses to add one whose CNAME does not already point here. A
 * domain either has a verdict from a lookup that ran, or it has none at all — which is the absence
 * of a `DomainStatus`, not a value of one. `waiting` is a record that is not there yet; `failed` is
 * one that is there and points elsewhere.
 */
export type DomainStatus = 'checking' | 'waiting' | 'verified' | 'failed';

/** `SocialProvider` in `cua`. Who a shopper can sign in with — a different, shorter list. */
export type LoginProvider = 'GOOGLE' | 'FACEBOOK' | 'GITHUB';

/** `PaymentType`. Only `STRIPE` and `PAYPAL` carry credentials (`PaymentType.attrs`). */
export type PaymentType = 'COD' | 'MANUAL_TRANSFER' | 'STRIPE' | 'PAYPAL';

export type LocaleCode = ConsoleLocale['code'];

/**
 * The CNAME an operator has to add at their registrar.
 *
 * No TTL. The fixture printed `3600`, which was the console telling the operator what to set on a
 * record in a zone the platform does not own and cannot read — a number with no source. Type, name and
 * value are the three the platform actually determines.
 */
export interface DnsRecord {
  readonly type: string;
  readonly name: string;
  readonly value: string;
}

/**
 * `ManagerStoreDomain`, plus the hostname it resolves to.
 *
 * `domain` is what is stored. `hostname` is what a browser types: for a `CUSTOM_DOMAIN` they are the
 * same, but a `SUB_DOMAIN` stores only its label and is reached at `{label}.{alis}-{pod}.{apex}` —
 * assembled on the client because no endpoint answers it. `null` when the pod lookup failed, which is
 * the difference between "we do not know the address" and a half-built one.
 *
 * Verification state is deliberately **not** here: it is the outcome of a lookup the operator asks for,
 * per domain, held by the facade — not a property of the record. The fixture stored it on the document
 * and could therefore only ever describe one custom domain.
 */
export interface StoreDomain {
  readonly domain: string;
  readonly type: DomainType;
  readonly hostname: string | null;
}

/**
 * The store's own identity, as `ReadableMerchantStore` + `MerchantStoreDetails` hold it.
 *
 * Split in two on purpose. The first block is what the platform actually stores and what a save
 * writes back. The second is what `Store Management.dc.html` designs and nothing anywhere records —
 * kept on the model so the section can still render those fields, disabled and labelled, rather
 * than silently dropping six controls the design asks for. `UNBACKED_DETAIL_FIELDS` below is the
 * list the section reads to decide which ones to disable.
 */
export interface StoreDetails {
  readonly name: string;
  readonly supportEmail: string;
  readonly supportPhone: string;
  readonly currency: string;
  /** The store's own default; `supportedLanguages` is the set it may be chosen from. */
  readonly language: string;
  readonly supportedLanguages: readonly string[];
  /** ISO country code on the wire — the form resolves it to a name. */
  readonly country: string;
  readonly address: StoreAddressFields;
  readonly theme: string;
  readonly colorTheme: string;
  /** `LocalDate` on the server: `YYYY-MM-DD`, never an instant. */
  readonly inBusinessSince: string;
  /** `MeasureUnit` — the enum also carries the weight values, which is the server's own muddle. */
  readonly dimensionUnit: string;
  /** `WeightUnit`. */
  readonly weightUnit: string;
  readonly requireLoginForOrderPlacement: boolean;
  readonly useCache: boolean;

  /*
   * Designed, never stored. Every one of these returns zero hits across `store-pod` and
   * `store-core`. See lessons.md, "Store management — six designed store fields do not exist"
   * and "Store management — a store has no published or maintenance state".
   */
  readonly legalName: string;
  readonly slug: string;
  readonly category: string;
  readonly timezone: string;
  readonly taxNumber: string;
  readonly shortDescription: string;
  readonly published: boolean;
  readonly maintenanceMode: boolean;
}

/**
 * `ReadableBaseAddress` → `BaseAddress`. Four fields plus the country on `StoreDetails`,
 * rather than the mockup's single `1180 Harrison St, Suite 400, San Francisco, CA 94103` line —
 * which cannot be saved, because the server stores the parts.
 */
export interface StoreAddressFields {
  readonly address: string;
  readonly city: string;
  readonly postalCode: string;
  /** Zone code, e.g. `CA`. Free text where a country has no zone list. */
  readonly stateProvince: string;
}

/**
 * The detail controls with nothing behind them, so the section can disable exactly these and the
 * save can refuse to send them. Keeping it as data rather than a flag per field means the list is
 * checkable against `PersistableMerchantStore` in one place.
 */
export const UNBACKED_DETAIL_FIELDS = [
  'legalName',
  'slug',
  'category',
  'timezone',
  'taxNumber',
  'shortDescription',
  'published',
  'maintenanceMode',
] as const;

export type UnbackedDetailField = (typeof UNBACKED_DETAIL_FIELDS)[number];


/**
 * What is known about a secret that never comes back from the server.
 *
 * `appSecret`, `secretKey` and `webhookSecret` are encrypted at rest and write-only, so the
 * console can only say how the stored one ends and when it was last changed.
 */
/** The gateways that carry credentials — `PaymentType.attrs` is non-empty for exactly these two. */
export const PAYMENT_TYPE_ICON: Readonly<Record<PaymentType, IconName>> = {
  COD: 'dollar',
  MANUAL_TRANSFER: 'receipt',
  STRIPE: 'creditCard',
  PAYPAL: 'creditCard',
};

export function isPaymentType(value: string): value is PaymentType {
  return value in PAYMENT_TYPE_ICON;
}

/** `PaymentType.attrs` is empty for these, so there is nothing to configure beyond the switch. */
export const PAYMENT_TYPES_WITHOUT_CREDENTIALS: readonly string[] = ['COD', 'MANUAL_TRANSFER'];



/**
 * The credential half of a gateway, as the endpoint actually returns it: in cleartext.
 *
 * `webhookUrl` is not returned by anything — it is assembled by the console from the route
 * `PublicPaymentWebhookApi` actually maps. See `paymentGateways` for the parts.
 */
export interface PaymentCredentials {
  readonly apiKey: string;
  readonly secretKey: string;
  readonly webhookSecret: string;
  /** Where the gateway should post its events, for pasting into Stripe or PayPal. */
  readonly webhookUrl: string;
}

/** `ReadablePaymentConfiguration`. Display copy resolved from `PAYMENT_TYPE_LABEL_KEY` / `PAYMENT_TYPE_DESCRIPTION_KEY`. */
export interface PaymentGatewayConfig {
  readonly paymentType: string;
  readonly icon: IconName;
  readonly enabled: boolean;
  /** `null` for the types whose `PaymentType.attrs` is empty — `COD` and `MANUAL_TRANSFER` are a switch and nothing else. */
  readonly credentials: PaymentCredentials | null;
  /** False for a gateway with no row at all, which decides create versus update on save. */
  readonly configured: boolean;
}

export const PAYMENT_TYPE_LABEL_KEY: Readonly<Record<PaymentType, string>> = {
  COD: 'storeSettings.paymentType.cod',
  MANUAL_TRANSFER: 'storeSettings.paymentType.manualTransfer',
  STRIPE: 'storeSettings.paymentType.stripe',
  PAYPAL: 'storeSettings.paymentType.paypal',
};

export const PAYMENT_TYPE_DESCRIPTION_KEY: Readonly<Record<PaymentType, string>> = {
  COD: 'storeSettings.paymentDescription.cod',
  MANUAL_TRANSFER: 'storeSettings.paymentDescription.manualTransfer',
  STRIPE: 'storeSettings.paymentDescription.stripe',
  PAYPAL: 'storeSettings.paymentDescription.paypal',
};

export interface StoreSettings {
  readonly storeName: string;
  readonly domains: readonly StoreDomain[];
  readonly details: StoreDetails;
  readonly payments: readonly PaymentGatewayConfig[];
  /**
   * The hostname a custom domain must CNAME to — `{alis}-{pod}.{apex}`.
   *
   * On the document rather than derived in a component because it takes two calls on two tiers to
   * work out, and three separate things read it: the subdomain's full hostname, the CNAME
   * instructions, and the DNS check's expected target. `null` when either call was refused.
   */
  readonly podTarget: string | null;
  /** The reference lists the selects are drawn from, fetched alongside the store itself. */
  readonly choices: SettingsChoices;
}

/**
 * What the server says the valid options are, rather than what the console guesses they are.
 *
 * Fetched with the store because the alternative is hardcoding four enums that the platform is
 * free to extend — `ColorTheme` alone has thirty values. Each list is independently optional: a
 * lookup that fails leaves its select showing the store's current value and nothing else, which is
 * still usable, instead of blanking the whole page.
 */
export interface SettingsChoices {
  readonly themes: readonly string[];
  readonly colorThemes: readonly string[];
  readonly languages: readonly string[];
  readonly socialLinkProviders: readonly string[];
}

export interface SettingsSection {
  readonly key: SettingsSectionKey;
  readonly labelKey: string;
  /** The tab-track's shorter label, e.g. "Branding" rather than "Store branding". */
  readonly shortLabelKey: string;
  readonly icon: IconName;
  /** Draws the amber dot: this section has something waiting on the operator. */
  readonly attention?: boolean;
}

/** The sub-nav, in the mockup's order. */
export const SECTIONS: readonly SettingsSection[] = [
  {key: 'domain', labelKey: 'storeSettings.section.domain', shortLabelKey: 'storeSettings.sectionShort.domain', icon: 'globe'},
  {key: 'details', labelKey: 'storeSettings.section.details', shortLabelKey: 'storeSettings.sectionShort.details', icon: 'building'},
  {key: 'payments', labelKey: 'storeSettings.section.payments', shortLabelKey: 'storeSettings.sectionShort.payments', icon: 'creditCard'},
];

export const SECTION_KEYS: readonly SettingsSectionKey[] = SECTIONS.map((section) => section.key);

export function isSectionKey(value: string): value is SettingsSectionKey {
  return (SECTION_KEYS as readonly string[]).includes(value);
}

/**
 * Domain state to its categorical tone.
 *
 * Replaces the mockup's dictionary of five hardcoded palettes: the state names a tone, and
 * the theme decides what that looks like.
 */
export const DOMAIN_STATUS_TONE: Readonly<Record<DomainStatus, Tone>> = {
  checking: 'blue',
  waiting: 'amber',
  verified: 'green',
  failed: 'red',
};

export interface DomainStatusCopy {
  readonly titleKey: string;
  readonly icon: IconName;
  readonly metaKey: string;
  /** Translated and interpolated with `{domain}` by the panel — see `DomainSection.body()`. */
  readonly bodyKey: string;
}

/** What each state says. `{domain}` is filled in by the panel. */
export const DOMAIN_STATUS_COPY: Readonly<Record<DomainStatus, DomainStatusCopy>> = {
  checking: {
    titleKey: 'storeSettings.domain.checking.title',
    icon: 'clock',
    metaKey: 'storeSettings.domain.checking.meta',
    bodyKey: 'storeSettings.domain.checking.body',
  },
  waiting: {
    titleKey: 'storeSettings.domain.waiting.title',
    icon: 'clock',
    metaKey: 'storeSettings.domain.waiting.meta',
    bodyKey: 'storeSettings.domain.waiting.body',
  },
  verified: {
    titleKey: 'storeSettings.domain.verified.title',
    icon: 'checkCircle',
    metaKey: 'storeSettings.domain.verified.meta',
    bodyKey: 'storeSettings.domain.verified.body',
  },
  failed: {
    titleKey: 'storeSettings.domain.failed.title',
    icon: 'xCircle',
    metaKey: 'storeSettings.domain.failed.meta',
    bodyKey: 'storeSettings.domain.failed.body',
  },
};

/** Tag beside a provider or gateway name. Never colour alone — the state is spelled out. */
export interface StateTag {
  readonly labelKey: string;
  readonly tone: Tone;
}

export const LOGIN_STATE_TAG: Readonly<Record<'on' | 'off', StateTag>> = {
  on: {labelKey: 'storeSettings.state.enabled', tone: 'green'},
  off: {labelKey: 'storeSettings.state.disabled', tone: 'slate'},
};

export const GATEWAY_STATE_TAG: Readonly<Record<'on' | 'off', StateTag>> = {
  on: {labelKey: 'storeSettings.state.live', tone: 'green'},
  off: {labelKey: 'storeSettings.state.off', tone: 'slate'},
};

/** The mockup's own hints, as numbers the validators and counters both read. */
export const META_DESCRIPTION_MAX = 160;
export const SHORT_DESCRIPTION_MAX = 160;

/** Lowercase letters, digits, hyphens and dots — no protocol, no path, no trailing slash. */
export const CUSTOM_DOMAIN_PATTERN = /^[a-z0-9]+(?:[.-][a-z0-9]+)*\.[a-z]{2,}$/;

export const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

/**
 * A pasted address reduced to the host a DNS record is actually about.
 *
 * `https://Shop.Example.com/collections/new` is what an operator copies out of the address bar, and
 * `shop.example.com` is the only part a CNAME can carry — a record named after a URL is not a record.
 * Rather than rejecting the paste and making them edit it, the field takes what they gave and keeps
 * the part that means something: scheme, credentials, port, path, query and fragment all go, and the
 * host is lowercased because DNS is case-insensitive and a mixed-case host looks like a typo.
 *
 * Deliberately not a validator. `CUSTOM_DOMAIN_PATTERN` still has to pass afterwards — this only
 * removes what is unambiguous, so `not a domain` stays wrong rather than being quietly mangled into
 * something that looks right.
 */
export function bareHostname(value: string): string {
  return value
    .trim()
    .replace(/^[a-z][a-z0-9+.-]*:\/\//i, '')
    .replace(/^[^/@]*@/, '')
    .split(/[/?#]/)[0]
    .replace(/:\d+$/, '')
    .toLowerCase();
}

/**
 * A phone number the console will accept.
 *
 * Deliberately permissive: an optional leading `+`, then digits with spaces, hyphens, dots and
 * parentheses between them. The server stores `phone` as a `String` with no format of its own, and
 * the stores on this platform trade from anywhere — a stricter rule would reject valid numbers to
 * enforce a convention nothing downstream relies on. What it does catch is the actual mistake:
 * a name, an address or an email typed into the phone field.
 */
export const PHONE_PATTERN = /^\+?[0-9](?:[0-9 ()./-]*[0-9])?$/;

/** Below this a "number" is a typo rather than a number, wherever it is dialled from. */
export const PHONE_MIN_DIGITS = 6;
