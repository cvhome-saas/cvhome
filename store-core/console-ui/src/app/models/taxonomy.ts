import type {Tone} from '@models/ui';

/**
 * The catalogue page's view models — categories, product types, brands and product groups.
 *
 * The wire shapes live in `@models/catalog`. This is what the four tabs bind to, after
 * `catalogue.api.service.ts` has picked one language out of each record's `descriptions[]` and
 * turned the server's paging envelopes into lists.
 *
 * The `catalog.ts` / `taxonomy.ts` split is the one Module 4 established with `checkout.ts` and
 * `orders.ts`: one file for what the server sends, one for what the screen shows, and a mapper
 * between them that is the only thing that knows both.
 */

/** Which tab of `/catalogue` is open. Part of the URL, so a tab is linkable. */
export type CatalogueTab = 'categories' | 'types' | 'brands' | 'options' | 'groups';

export const CATALOGUE_TABS: readonly CatalogueTab[] = [
  'categories',
  'types',
  'brands',
  'options',
  'groups',
];

/** Whether a string names a tab — guards the route param before it reaches the facade. */
export function isCatalogueTab(value: string | null | undefined): value is CatalogueTab {
  return value !== null && value !== undefined && (CATALOGUE_TABS as readonly string[]).includes(value);
}

/**
 * One language's copy for anything in the catalogue.
 *
 * Deliberately not `CategoryDescription` and its six siblings: they are the same seven fields under
 * seven names, and every editor on this page writes the same subset of them. Keeping one view model
 * is what lets the locale chips, the "not translated" flag and the SEO counters be written once.
 */
export interface LocalisedCopy {
  readonly language: string;
  readonly name: string;
  readonly description: string;
  readonly friendlyUrl: string;
  readonly title: string;
  readonly metaDescription: string;
  readonly highlights: string;
  readonly keyWords: string;
}

/** An empty description for a language nothing has been written in yet. */
export function emptyCopy(language: string): LocalisedCopy {
  return {
    language,
    name: '',
    description: '',
    friendlyUrl: '',
    title: '',
    metaDescription: '',
    highlights: '',
    keyWords: '',
  };
}

/**
 * One category, as the tree draws it.
 *
 * `productCount` is the category's **own** products; `totalCount` adds its descendants'. Both are
 * kept because they answer different questions — "what is in here" and "what is under here" — and
 * the tree shows the rolled-up figure, which is the one an operator deciding whether a branch is
 * worth keeping actually wants.
 */
export interface CategoryNode {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly visible: boolean;
  readonly sortOrder: number;
  readonly depth: number;
  readonly parentId: number | null;
  readonly productCount: number;
  readonly totalCount: number;
  readonly copy: readonly LocalisedCopy[];
  readonly children: readonly CategoryNode[];
}

/**
 * One brand.
 *
 * **A name, a description and a code — that is the whole record.** No logo and no publish flag,
 * because `ReadableManufacturer` carries neither. And no sort order: the field is on the DTO, but
 * `PersistableManufacturerPopulator` sets only `code` and the per-language `name`, `description` and
 * `languageCode`, so a value sent for `order` is read and dropped. Nor is there a slug —
 * `manufacturer_description` has no `sef_url` column at all. Both were on this editor and both did
 * nothing; see lessons.md, "Catalogue — a brand persists only its name and its description".
 */
export interface BrandCard {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly description: string;
  readonly copy: readonly LocalisedCopy[];
  /** First letters of the first two words. The card's mark, since there is no logo to show. */
  readonly initials: string;
}

/** One product type. No attribute list — see lessons.md. */
export interface TypeCard {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly description: string;
  readonly visible: boolean;
  readonly allowAddToCart: boolean;
  readonly copy: readonly LocalisedCopy[];
}

/**
 * One name in one language — the option editor's whole per-language payload.
 *
 * Options carry none of `LocalisedCopy`'s slug/SEO fields: `product_option_description` is a
 * `NamedEntity` of which only `name` renders anywhere, so the view model says so instead of
 * dragging six empty strings around.
 */
export interface OptionName {
  readonly language: string;
  readonly name: string;
}

/** One value of a store option — "Red" under Color. Ids are store-wide; faceting keys on them. */
export interface OptionValueCard {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly copy: readonly OptionName[];
}

/**
 * One store option (Color, Size, …), as the Options tab lists it.
 *
 * Store-wide and reusable: defined once, translated once, then assigned per product in the product
 * form's variants step. Deleting one is refused (409) while any product still uses it.
 */
export interface OptionCard {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly sortOrder: number;
  readonly copy: readonly OptionName[];
  readonly values: readonly OptionValueCard[];
}

/** One product group, and the products in it. */
export interface GroupRow {
  readonly code: string;
  readonly name: string;
  readonly active: boolean;
  readonly copy: readonly LocalisedCopy[];
  readonly members: readonly GroupMember[];
}

export interface GroupMember {
  readonly id: number;
  readonly name: string;
  readonly sku: string;
}

/** Everything the four tabs need, in one load. */
export interface CatalogueSnapshot {
  readonly categories: readonly CategoryNode[];
  readonly brands: readonly BrandCard[];
  readonly types: readonly TypeCard[];
  readonly options: readonly OptionCard[];
  readonly groups: readonly GroupRow[];
  /** The languages this store's copy can be written in, from the store's own supported set. */
  readonly languages: readonly string[];
  /** Which of the three optional legs failed, so a tab can say so instead of looking empty. */
  readonly unavailable: readonly CatalogueTab[];
}

/** The tone a tab's count chip carries. One hue per tab, so the strip reads as a set. */
export const TAB_TONE: Readonly<Record<CatalogueTab, Tone>> = {
  categories: 'green',
  types: 'blue',
  brands: 'violet',
  options: 'amber',
  groups: 'cyan',
};

/**
 * The SEO field lengths the storefront's markup is built for.
 *
 * **Advisory, and softer than the column.** These are where a search engine stops displaying, not
 * where the database stops accepting — the counters turn amber here while `TITLE_MAX` and
 * `META_DESCRIPTION_MAX` below are what the form actually refuses.
 */
export const SEO_TITLE_LIMIT = 70;
export const SEO_DESCRIPTION_LIMIT = 160;

/* ------------------------------------------------------------------- column bounds ---- */

/**
 * What each column will actually hold, from
 * `store-pod/catalog/catalog-service/src/main/resources/init-sql/schema.sql`.
 *
 * These are hard limits, not preferences. Nothing between the console and the database validates
 * length, so an over-long value is a driver error surfaced as a 500 — which reads to the operator as
 * the console breaking rather than as a field being too long. The four description tables agree on
 * every one of these except the slug, which is the reason `CATEGORY_SLUG_MAX` exists separately.
 */
export const NAME_MAX = 120;
export const TITLE_MAX = 100;
export const META_DESCRIPTION_MAX = 255;
export const KEYWORDS_MAX = 255;
export const HIGHLIGHTS_MAX = 255;
/** `product_description.sef_url`. */
export const SLUG_MAX = 255;
/** `category_description.sef_url` — half the product's, and the one that catches people out. */
export const CATEGORY_SLUG_MAX = 120;
/** `category.code`, `manufacturer.code`, `product_group.code`. */
export const CODE_MAX = 100;
/** `product_type.prd_type_code`, which is wider than its three siblings for no stated reason. */
export const TYPE_CODE_MAX = 255;
/** `product.sku`. */
export const SKU_MAX = 255;

/**
 * How long a uniqueness check waits before asking the server.
 *
 * One value for every such check in the console. The three that existed before this used 250, 400
 * and 600ms with no reason for the difference recorded anywhere.
 */
export const UNIQUENESS_DEBOUNCE_MS = 400;

/** Picks the copy for a language, falling back to the first entry the server sent. */
export function copyFor(
  copy: readonly LocalisedCopy[],
  language: string,
): LocalisedCopy | undefined {
  return copy.find((entry) => entry.language === language) ?? copy[0];
}

/**
 * The name to show, in the reader's language where there is one.
 *
 * Falls back to the first description rather than to a placeholder, because a category named only
 * in Arabic is still named — showing "Untitled" for it would be wrong, not merely unhelpful. Only a
 * record with no descriptions at all has no name, and the caller supplies what to say then.
 */
export function nameIn(copy: readonly LocalisedCopy[], language: string, fallback: string): string {
  return copyFor(copy, language)?.name || copy.find((entry) => entry.name)?.name || fallback;
}
