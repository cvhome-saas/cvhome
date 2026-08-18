import type {ConsoleLocale} from '@core/i18n/locale.service';
import type {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '@shared/ui/tone';

/**
 * The store's settings surface.
 *
 * Shaped after the backend contracts it will eventually be served from —
 * `ReadableMerchantStore` + `MerchantStoreDetails`, `ManagerStoreDomain`,
 * `ReadableSliderImage`, `SocialLink`, `ReadableSocialLoginConfig` and
 * `ReadablePaymentConfiguration` — so swapping the mock for HTTP is a rename rather than a
 * rewrite. Provider lists are the real enums, not the mockup's inventions.
 */

export type SettingsSectionKey =
  | 'branding'
  | 'home'
  | 'domain'
  | 'social'
  | 'slider'
  | 'details'
  | 'social-login'
  | 'payments';

/** `DomainType` in `commons`. A store always has the first and may add the second. */
export type DomainType = 'SUB_DOMAIN' | 'CUSTOM_DOMAIN';

/**
 * Where a custom domain stands. `unverified` is a domain that has been typed but never
 * checked; `failed` is a check that came back wrong rather than empty.
 */
export type DomainStatus = 'unverified' | 'checking' | 'waiting' | 'verified' | 'failed';

/** `SocialProvider` in `store-commons/commons`. The storefront footer's links. */
export type SocialLinkProvider = 'FACEBOOK' | 'X' | 'TIKTOK' | 'INSTAGRAM' | 'GITHUB';

/** `SocialProvider` in `cua`. Who a shopper can sign in with — a different, shorter list. */
export type LoginProvider = 'GOOGLE' | 'FACEBOOK' | 'GITHUB';

/** `PaymentType`. Only `STRIPE` and `PAYPAL` carry credentials (`PaymentType.attrs`). */
export type PaymentType = 'COD' | 'MANUAL_TRANSFER' | 'STRIPE' | 'PAYPAL';

export type LocaleCode = ConsoleLocale['code'];

/** `ReadableImage`, reduced to what the page shows. */
export interface StoredImage {
  readonly name: string;
  /** Absent until an upload completes — the zone shows its placeholder instead. */
  readonly url: string | null;
}

/**
 * The store's marketing images.
 *
 * The mockup's upload-progress bar is gone: an upload is a single POST that either completes or
 * fails, and the section says which. A percentage that only ever read 0 or 100 was decoration.
 */
export interface BrandingSettings {
  readonly logo: StoredImage | null;
  readonly banner: StoredImage | null;
}

/** One language's landing copy. Per-language because `supportedLanguages` drives the track. */
export interface HomePageCopy {
  readonly title: string;
  readonly text: string;
  readonly metaDescription: string;
  readonly tags: readonly string[];
}

export interface DnsRecord {
  readonly type: string;
  readonly name: string;
  readonly value: string;
  readonly ttl: number;
}

/** `ManagerStoreDomain`, plus the verification state the console has to show. */
export interface StoreDomain {
  readonly domain: string;
  readonly type: DomainType;
  readonly status: DomainStatus;
  /** The CNAME the operator has to add. Only custom domains need one. */
  readonly record: DnsRecord | null;
}

/** `SocialLink(provider, url)`, with the mark the row is drawn with. Display name is a brand name — resolved from `SOCIAL_LINK_LABEL_KEY`, not carried on the record. */
export interface SocialLinkSetting {
  readonly provider: SocialLinkProvider;
  readonly icon: IconName;
  readonly url: string;
}

/** Brand names. Kept translated (rather than hardcoded) so a right-to-left reader still sees them presented consistently — the names themselves do not change across languages. */
export const SOCIAL_LINK_LABEL_KEY: Readonly<Record<SocialLinkProvider, string>> = {
  FACEBOOK: 'storeSettings.socialProvider.facebook',
  X: 'storeSettings.socialProvider.x',
  TIKTOK: 'storeSettings.socialProvider.tiktok',
  INSTAGRAM: 'storeSettings.socialProvider.instagram',
  GITHUB: 'storeSettings.socialProvider.github',
};

/** `ReadableSliderImage(priority, name, url)`, plus what the carousel row displays. */
export interface SliderSlide {
  readonly id: string;
  readonly priority: number;
  readonly name: string;
  /** Dimensions, weight and format, as the row's second line: `1600×640 · 248 KB · JPG`. */
  readonly meta: string;
  readonly link: string;
  readonly state: 'LIVE' | 'SCHEDULED';
  readonly url: string | null;
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
export interface SecretHint {
  /** The last four characters, or `null` when nothing is stored. */
  readonly endsWith: string | null;
  readonly lastRotated: string | null;
}

/** `ReadableSocialLoginConfig`, with `appSecret` reduced to a hint. Display name resolved from `LOGIN_PROVIDER_LABEL_KEY`. */
export interface SocialLoginConfig {
  readonly providerId: LoginProvider;
  readonly icon: IconName;
  readonly appId: string;
  readonly appSecret: SecretHint;
  readonly callbackUrl: string;
  readonly enabled: boolean;
}

export const LOGIN_PROVIDER_LABEL_KEY: Readonly<Record<LoginProvider, string>> = {
  GOOGLE: 'storeSettings.loginProvider.google',
  FACEBOOK: 'storeSettings.loginProvider.facebook',
  GITHUB: 'storeSettings.loginProvider.github',
};

/** The credential half of a gateway. Absent on the types that have no `attrs`. */
export interface PaymentCredentials {
  readonly apiKey: string;
  readonly secretKey: SecretHint;
  readonly webhookSecret: SecretHint;
  readonly webhookUrl: string;
}

/** `ReadablePaymentConfiguration`, with both secrets reduced to hints. Display copy resolved from `PAYMENT_TYPE_LABEL_KEY` / `PAYMENT_TYPE_DESCRIPTION_KEY`. */
export interface PaymentGatewayConfig {
  readonly paymentType: PaymentType;
  readonly icon: IconName;
  readonly enabled: boolean;
  /** `null` for `COD` and `MANUAL_TRANSFER`, which are a switch and nothing else. */
  readonly credentials: PaymentCredentials | null;
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
  readonly branding: BrandingSettings;
  /** Keyed by console locale. A missing language is untranslated, and falls back to English. */
  readonly home: Readonly<Partial<Record<LocaleCode, HomePageCopy>>>;
  readonly domains: readonly StoreDomain[];
  readonly socialLinks: readonly SocialLinkSetting[];
  readonly slides: readonly SliderSlide[];
  readonly details: StoreDetails;
  readonly socialLogin: readonly SocialLoginConfig[];
  readonly payments: readonly PaymentGatewayConfig[];
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
  {key: 'branding', labelKey: 'storeSettings.section.branding', shortLabelKey: 'storeSettings.sectionShort.branding', icon: 'palette'},
  {key: 'home', labelKey: 'storeSettings.section.home', shortLabelKey: 'storeSettings.sectionShort.home', icon: 'desktop'},
  {key: 'domain', labelKey: 'storeSettings.section.domain', shortLabelKey: 'storeSettings.sectionShort.domain', icon: 'globe'},
  {key: 'social', labelKey: 'storeSettings.section.social', shortLabelKey: 'storeSettings.sectionShort.social', icon: 'share'},
  {key: 'slider', labelKey: 'storeSettings.section.slider', shortLabelKey: 'storeSettings.sectionShort.slider', icon: 'images'},
  {key: 'details', labelKey: 'storeSettings.section.details', shortLabelKey: 'storeSettings.sectionShort.details', icon: 'building'},
  {key: 'social-login', labelKey: 'storeSettings.section.socialLogin', shortLabelKey: 'storeSettings.sectionShort.socialLogin', icon: 'signIn'},
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
  unverified: 'slate',
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
  unverified: {
    titleKey: 'storeSettings.domain.unverified.title',
    icon: 'questionCircle',
    metaKey: 'storeSettings.domain.unverified.meta',
    bodyKey: 'storeSettings.domain.unverified.body',
  },
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

export const SLIDE_STATE_TONE: Readonly<Record<SliderSlide['state'], Tone>> = {
  LIVE: 'green',
  SCHEDULED: 'amber',
};

/** How many slides the carousel holds, for the "3 of 8 slots used" line. */
export const SLIDER_CAPACITY = 8;

/** The mockup's own hints, as numbers the validators and counters both read. */
export const HOME_TITLE_MAX = 60;
export const META_DESCRIPTION_MIN = 150;
export const META_DESCRIPTION_MAX = 160;
export const SHORT_DESCRIPTION_MAX = 160;

/** Lowercase letters, digits, hyphens and dots — no protocol, no path, no trailing slash. */
export const CUSTOM_DOMAIN_PATTERN = /^[a-z0-9]+(?:[.-][a-z0-9]+)*\.[a-z]{2,}$/;

export const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
