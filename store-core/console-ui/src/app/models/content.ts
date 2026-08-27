/**
 * Wire DTOs of the content service (`store-pod/content`), private console API.
 *
 * Console-native: written against the content platform, not ported from seller-core. Field names
 * mirror the Java DTOs in `content-commons` (`PersistableContent`, `ContentTranslation`,
 * `ReadableContentRow`, `ContentSummary`, `Snippet`, …) so a change on either side is a grep away.
 */

import type {PageT} from '@models/page';

/** The content types that have a workflow and a console list. */
export type ContentListType = 'pages' | 'posts' | 'banners' | 'faq' | 'policies';

/** Every tab of the Content management hub. */
export type ContentTab = ContentListType | 'branding' | 'media' | 'menus';

export const CONTENT_LIST_TYPES: readonly ContentListType[] = ['pages', 'posts', 'banners', 'faq', 'policies'];

export const CONTENT_TABS: readonly ContentTab[] =
  ['pages', 'posts', 'banners', 'faq', 'media', 'menus', 'policies', 'branding'];

export type ContentType = 'PAGE' | 'SECTION' | 'POST' | 'BANNER' | 'FAQ' | 'POLICY';

export type ContentStatus = 'DRAFT' | 'REVIEW' | 'SCHEDULED' | 'PUBLISHED' | 'ARCHIVED';

export const CONTENT_STATUSES: readonly ContentStatus[] = ['DRAFT', 'REVIEW', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'];

/** How complete one locale of an item is. */
export type TranslationState = 'MISSING' | 'DRAFT' | 'TRANSLATED' | 'STALE';

export type BannerPlacement = 'HERO' | 'CAROUSEL' | 'COLLECTION' | 'STRIP';

export const BANNER_PLACEMENTS: readonly BannerPlacement[] = ['HERO', 'CAROUSEL', 'COLLECTION', 'STRIP'];

export type PolicyType = 'TERMS' | 'PRIVACY' | 'RETURNS' | 'SHIPPING' | 'COOKIES' | 'CUSTOM';

export const POLICY_TYPES: readonly PolicyType[] = ['TERMS', 'PRIVACY', 'RETURNS', 'SHIPPING', 'COOKIES', 'CUSTOM'];

export type PolicyVersionStatus = 'DRAFT' | 'LIVE' | 'ARCHIVED';

export type MenuHandle = 'MAIN' | 'FOOTER';

export type MenuTargetKind = 'PAGE' | 'CATEGORY' | 'PRODUCT' | 'POLICY' | 'BLOG_INDEX' | 'FAQ_INDEX' | 'URL';

export const MENU_TARGET_KINDS: readonly MenuTargetKind[] = [
  'PAGE',
  'CATEGORY',
  'PRODUCT',
  'POLICY',
  'BLOG_INDEX',
  'FAQ_INDEX',
  'URL',
];

export type MediaKind = 'IMAGE' | 'VIDEO' | 'DOCUMENT' | 'ARCHIVE' | 'VECTOR';

export const MEDIA_KINDS: readonly MediaKind[] = ['IMAGE', 'VIDEO', 'DOCUMENT', 'ARCHIVE', 'VECTOR'];

export type BulkAction = 'PUBLISH' | 'UNPUBLISH' | 'ARCHIVE' | 'DELETE';

/** The status transitions the API exposes as `POST …/{id}/<action>`. */
export type TransitionAction = 'publish' | 'unpublish' | 'submit-review' | 'archive' | 'restore';

/**
 * One locale of a content item — maps 1:1 onto the server's `content_description` row (`title` is
 * the row's `name`, `body` its `description`). Which fields matter depends on the item type.
 */
export interface ContentTranslation {
  readonly id?: number;
  readonly language: string;
  readonly state?: TranslationState;
  readonly title?: string;
  readonly body?: string;
  readonly excerpt?: string;
  readonly friendlyUrl?: string;
  readonly metaTitle?: string;
  readonly metaDescription?: string;
  /** Comma-separated search keywords. */
  readonly keywords?: string;
  readonly altText?: string;
  readonly ctaLabel?: string;
  readonly subtitle?: string;
}

export interface LocaleState {
  readonly code: string;
  readonly state: TranslationState;
}

export interface ContentAudit {
  readonly createdAt?: string;
  readonly createdBy?: string;
  readonly updatedAt?: string;
  readonly updatedBy?: string;
}

/** One row of a console list. */
export interface ContentRow {
  readonly id: number;
  readonly type: ContentType;
  readonly slug: string;
  readonly title: string;
  readonly subtitle: string;
  readonly status: ContentStatus;
  readonly publishAt?: string | null;
  readonly locales: readonly LocaleState[];
  readonly updatedAt?: string | null;
  readonly updatedBy?: string | null;
}

/** The fields every workflow item carries on write. Type-specific DTOs extend it. */
export interface PersistableContent {
  readonly id?: number;
  /** The optimistic lock: send what was read; a stale one is a 409 `CONTENT.VERSION.CONFLICT`. */
  readonly version?: number;
  readonly slug: string;
  readonly translations: readonly ContentTranslation[];
  readonly publishAt?: string | null;
  readonly unpublishAt?: string | null;
  readonly noindex?: boolean;
  readonly canonicalUrl?: string | null;
  readonly ogMediaId?: number | null;
  readonly sortOrder?: number | null;
}

/** The read-only quartet every readable item adds. */
export interface ReadableContentMeta {
  readonly type: ContentType;
  readonly status: ContentStatus;
  readonly locales: readonly LocaleState[];
  readonly audit: ContentAudit;
}

export interface PersistablePage extends PersistableContent {
  readonly parentId?: number | null;
  readonly showInFooter?: boolean;
}

export type ReadablePage = PersistablePage & ReadableContentMeta;

export interface PersistablePost extends PersistableContent {
  readonly heroMediaId?: number | null;
  readonly categoryIds?: readonly number[];
  readonly tags?: readonly string[];
  readonly authorName?: string | null;
  readonly featured?: boolean;
}

export interface ReadablePost extends PersistablePost, ReadableContentMeta {
  readonly readingMinutes?: number;
  readonly heroMediaUrl?: string | null;
}

export interface BannerTarget {
  readonly kind: 'COLLECTION' | 'PRODUCT' | 'PAGE' | 'URL';
  readonly value: string;
}

export interface BannerArtwork {
  readonly desktopMediaId?: number | null;
  readonly mobileMediaId?: number | null;
  readonly mobileCrop?: string | null;
}

export interface BannerTheme {
  readonly textColor?: string | null;
  readonly overlayOpacity?: number | null;
  readonly alignment?: string | null;
}

export interface PersistableBanner extends PersistableContent {
  readonly placement: BannerPlacement;
  readonly startsAt?: string | null;
  readonly endsAt?: string | null;
  readonly target?: BannerTarget | null;
  readonly artwork?: BannerArtwork | null;
  readonly theme?: BannerTheme | null;
  readonly loggedInOnly?: boolean;
}

export interface ReadableBanner extends PersistableBanner, ReadableContentMeta {
  readonly desktopUrl?: string | null;
  readonly mobileUrl?: string | null;
}

export interface PersistableFaq extends PersistableContent {
  readonly groupId?: number | null;
  readonly position?: number | null;
  readonly keywords?: readonly string[];
  readonly showInCheckoutHelp?: boolean;
}

export interface ReadableFaq extends PersistableFaq, ReadableContentMeta {
  readonly groupName?: string | null;
}

export interface FaqGroup {
  readonly id?: number;
  readonly key: string;
  readonly names: Readonly<Record<string, string>>;
  readonly position?: number;
  readonly entryCount?: number;
}

export interface FaqReorder {
  readonly id: number;
  readonly groupId: number;
  readonly position: number;
}

export interface PersistablePolicy extends PersistableContent {
  readonly policyType: PolicyType;
  readonly jurisdiction?: string | null;
  readonly effectiveFrom?: string | null;
  readonly requiresAcceptance?: boolean;
  readonly notifyCustomers?: boolean;
  readonly showInFooter?: boolean;
  readonly showAtCheckout?: boolean;
  readonly showAtSignup?: boolean;
}

export interface ReadablePolicyVersion {
  readonly version: number;
  readonly status: PolicyVersionStatus;
  readonly note?: string | null;
  readonly effectiveFrom?: string | null;
  readonly publishedAt?: string | null;
  readonly publishedBy?: string | null;
  readonly translations?: readonly ContentTranslation[];
}

export interface ReadablePolicy extends PersistablePolicy, ReadableContentMeta {
  readonly liveVersion: number;
  readonly versions: readonly ReadablePolicyVersion[];
}

export interface PolicyCompliance {
  readonly type: PolicyType;
  readonly requiredBy: readonly string[];
  readonly status: ContentStatus | null;
  readonly id: number | null;
}

export interface PublishPolicyVersionRequest {
  readonly effectiveFrom?: string | null;
  readonly note?: string | null;
}

export interface PostCategory {
  readonly id?: number;
  readonly slug: string;
  readonly names: Readonly<Record<string, string>>;
  readonly position?: number;
  readonly postCount?: number;
}

export interface MenuTarget {
  readonly kind: MenuTargetKind;
  readonly value?: string | null;
  /** Set by the server when an internal target no longer resolves. */
  readonly broken?: boolean | null;
}

export interface MenuItem {
  readonly id?: number;
  readonly position?: number;
  readonly labels: Readonly<Record<string, string>>;
  readonly target: MenuTarget;
  readonly openInNewTab?: boolean;
  readonly visible?: boolean;
  readonly children: readonly MenuItem[];
}

export interface Menu {
  readonly id?: number;
  readonly handle: MenuHandle;
  readonly names?: Readonly<Record<string, string>>;
  readonly items: readonly MenuItem[];
  readonly itemCount?: number;
}

export interface MediaFolder {
  readonly id?: number;
  readonly name: string;
  readonly key?: string;
  readonly position?: number;
  readonly system?: boolean;
  readonly fileCount?: number;
}

export interface MediaUsage {
  readonly ownerKind: MediaOwnerKind;
  /** The owner's id within its kind — a content id as text, the store id, a product id. */
  readonly ownerRef: string;
  /** Set only when {@link ownerKind} is `CONTENT`. */
  readonly itemType?: ContentType | null;
  readonly itemId?: number | null;
  readonly itemTitle?: string | null;
  readonly field: string;
}

export interface MediaAsset {
  readonly id: number;
  readonly filename: string;
  readonly originalFilename: string;
  readonly mimeType: string;
  readonly kind: MediaKind;
  readonly bytes: number;
  readonly width?: number | null;
  readonly height?: number | null;
  readonly url: string;
  readonly folderId?: number | null;
  readonly altTexts?: Readonly<Record<string, string>>;
  readonly title?: string | null;
  readonly tags?: readonly string[];
  readonly uploadedAt?: string;
  readonly uploadedBy?: string | null;
  readonly usageCount: number;
  readonly usage?: readonly MediaUsage[] | null;
}

export interface PersistableMediaAsset {
  readonly folderId?: number | null;
  readonly altTexts?: Readonly<Record<string, string>>;
  readonly title?: string | null;
  readonly tags?: readonly string[];
}

/**
 * A store-level text fragment the storefront reads by code (the legacy "content box"). No workflow:
 * always live, `visible` toggles it.
 */
/** What holds a reference to a media asset. Orthogonal to {@link ContentType}, not a replacement for it. */
export type MediaOwnerKind = 'CONTENT' | 'SITE_SETTINGS' | 'PRODUCT' | 'CATEGORY' | 'BRAND';

/** A media asset resolved for display: the id to write back, and the URL to show. */
export interface MediaRef {
  readonly id: number;
  readonly url: string;
  readonly alt: string | null;
  readonly width: number | null;
  readonly height: number | null;
}

/** The store's brand imagery. Every slot is optional. */
export interface SiteBranding {
  readonly logo: MediaRef | null;
  readonly logoDark: MediaRef | null;
  readonly favicon: MediaRef | null;
  readonly og: MediaRef | null;
}

export interface SocialLink {
  readonly provider: string;
  readonly url: string;
}

/**
 * How the store looks: brand imagery, social links and site-level SEO.
 *
 * These used to be spread across merchant (logo, banner, slider images, social links) and the legacy content
 * snippets (`meta-title`, `meta-description`). This is the only place they live now. A `null` media slot
 * clears it — which is how a logo finally becomes removable.
 */
export interface SiteSettings {
  readonly logoMediaId: number | null;
  readonly logoDarkMediaId: number | null;
  readonly faviconMediaId: number | null;
  readonly ogMediaId: number | null;
  /** Per-field, per-locale: `{metaTitle: {en: '…', ar: '…'}, metaDescription: {…}}`. */
  readonly seo: Readonly<Record<string, Readonly<Record<string, string>>>>;
  readonly socialLinks: readonly SocialLink[];
  readonly branding?: SiteBranding;
  readonly updatedAt?: string | null;
  readonly updatedBy?: string | null;
}

/** What a home-page section renders. */
export type HomeSectionKind =
  'PRODUCT_GROUP' | 'CATEGORY_GRID' | 'BANNER_REF' | 'RICH_TEXT' | 'IMAGE' | 'POST_FEED' | 'FAQ_REF';

/**
 * A block on the store's home page. The page used to be a hard-coded list of four product rails in the
 * storefront's own code, so a seller could neither reorder it nor put anything else on it.
 */
export interface PersistableSection extends PersistableContent {
  readonly kind: HomeSectionKind;
  readonly targetValue?: string | null;
  readonly mediaId?: number | null;
  readonly itemLimit?: number | null;
  readonly layout?: string | null;
  readonly cta?: MenuTarget | null;
}

export interface ReadableSection extends PersistableSection {
  readonly type: ContentType;
  readonly status: ContentStatus;
  readonly locales: readonly LocaleState[];
  readonly audit?: ContentAudit;
  readonly imageUrl?: string | null;
}

export interface ContentSummary {
  readonly publishedItems: number;
  readonly drafts: {readonly total: number; readonly staleOver30Days: number};
  readonly awaitingTranslation: {readonly total: number; readonly byLocale: Readonly<Record<string, number>>};
  readonly media: {readonly bytesUsed: number; readonly bytesQuota: number; readonly fileCount: number};
  readonly counts: Readonly<Record<string, number>>;
}

export interface SavedContent {
  readonly id: number;
  readonly status: ContentStatus;
  readonly version: number;
}

export interface PublishRequest {
  readonly publishAt?: string | null;
  readonly unpublishAt?: string | null;
}

export interface BulkResult {
  readonly id: number;
  readonly ok: boolean;
  readonly errorCode?: string | null;
  readonly message?: string | null;
}

export interface ReadableRevision {
  readonly version: number;
  readonly author?: string | null;
  readonly createdAt: string;
}

export interface Redirect {
  readonly id: number;
  readonly fromPath: string;
  readonly toPath: string;
  readonly createdAt: string;
}

export interface PreviewToken {
  readonly token: string;
  readonly path: string;
}

/** The filters of a console list; `status` null = all, `locale` null = all. */
export interface ContentListQuery {
  readonly status: ContentStatus | null;
  readonly locale: string | null;
  readonly state: TranslationState | null;
  readonly q: string;
}

export const NO_CONTENT_QUERY: ContentListQuery = {status: null, locale: null, state: null, q: ''};

export type ContentPage = PageT<ContentRow>;
