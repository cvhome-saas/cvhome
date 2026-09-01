import type {PageT} from '@models/page';
import type {LocalisedCopy} from '@models/taxonomy';

/**
 * The product list's and the product form's view models.
 *
 * The wire shapes live in `@models/catalog`; this is what the two features bind to.
 *
 * **What is not here, and why.** No split between stock held and stock reserved, no replenishment
 * level, no stock value, no location, no cost and no margin. The template draws all of them and the platform models none:
 * `InventoryEntity` is a quantity and two free-text region strings, and there is no cost field
 * anywhere on a product — only a sale price. Every one of those has an entry in lessons.md rather
 * than a plausible number in this file.
 */

/** Which slice of the catalogue the list is showing. `available` on `ProductCriteria`, or nothing. */
export type ProductTab = 'all' | 'available' | 'unavailable';

export const PRODUCT_TABS: readonly ProductTab[] = ['all', 'available', 'unavailable'];

/**
 * What the operator has narrowed the list to.
 *
 * Three filters, not the four the plan expected. `ProductCriteria.productName` is accepted by the
 * binder and never read — `ProductRepository`'s predicate builder reads `sku`, `manufacturerId`,
 * `categoryIds` and `available` and nothing else — so a name search would return the whole catalogue
 * while looking as though it had filtered. See lessons.md, "Catalogue — the product-name filter is
 * accepted and ignored".
 *
 * `sku` is a substring match (`LIKE %sku%`), so the box works as a search rather than a lookup.
 */
export interface ProductFilters {
  readonly sku: string;
  readonly categoryId: number | null;
  readonly brandId: number | null;
}

export const NO_FILTERS: ProductFilters = {sku: '', categoryId: null, brandId: null};

/**
 * One product, as a table row.
 *
 * `price` and `quantity` are held as numbers rather than rendered strings so the row survives a
 * language change and so the inline editor has something to start from. `currency` comes from the
 * store rather than the product — there is no per-product currency; see lessons.md.
 */
export interface ProductRow {
  readonly id: number;
  readonly name: string;
  /** The default variant's sku — the row's price and stock cells describe this sku. */
  readonly sku: string;
  /** How many variants the product owns. `> 1` disables inline edit; the matrix lives in the form. */
  readonly variantCount: number;
  /** The categories the product is in, already named in the reader's language. */
  readonly categories: readonly string[];
  readonly brand: string | null;
  readonly price: number | null;
  readonly quantity: number;
  readonly available: boolean;
  readonly shipeable: boolean;
  /** The default image's URL, or `null`. Shown as a thumbnail, with a fallback glyph. */
  readonly imageUrl: string | null;
}

/** What the list page reads in one response. */
export interface ProductsSnapshot {
  readonly page: PageT<ProductRow>;
  /** The category tree, flattened and indented, for the filter select. */
  readonly categories: readonly ProductFilterOption[];
  readonly brands: readonly ProductFilterOption[];
  /** The store's currency, for rendering prices. */
  readonly currency: string | null;
}

export interface ProductFilterOption {
  readonly id: number;
  readonly label: string;
}

/** One inline edit, as the row emits it. All three fields travel together — see `ProductService.patch`. */
export interface InlineProductEdit {
  readonly id: number;
  readonly price: number | null;
  readonly quantity: number;
  readonly available: boolean;
}

/* ------------------------------------------------------------------------ the form ---- */

/** The wizard's five steps, in order. */
export type ProductStep = 'essentials' | 'media' | 'pricing' | 'variants' | 'organize';

export const PRODUCT_STEPS: readonly ProductStep[] = [
  'essentials',
  'media',
  'pricing',
  'variants',
  'organize',
];

export function isProductStep(value: string | null | undefined): value is ProductStep {
  return value !== null && value !== undefined && (PRODUCT_STEPS as readonly string[]).includes(value);
}

/**
 * The units the specification is written in.
 *
 * The server's own enums — `MeasureUnit` and `WeightUnit` — and the two values on this page that
 * need Module 4's known-set guard, because Transloco throws on a missing key and a value added
 * server-side would otherwise take the form down.
 */
/*
 * The server's own enums, spelling and case included.
 *
 * `WeightUnitOfMeasure` is `g, kg, l, lb, T` and `DimensionUnitOfMeasure` is `cm, cu, ft, in, m` —
 * lowercase, except the ton. This console previously declared an invented uppercase pair,
 * `['KG','LB']` and `['CM','IN']`, which was wrong twice over: the value never matched what the
 * server sent, so the unit field rendered blank on every seeded product, and a save would have
 * posted `KG` at an enum that has no such constant. Three of the five weights and three of the five
 * dimensions were simply unreachable.
 *
 * `l` sitting in a weight enum is the server's choice, not a typo here.
 */
export const WEIGHT_UNITS: readonly string[] = ['g', 'kg', 'l', 'lb', 'T'];
export const DIMENSION_UNITS: readonly string[] = ['cm', 'cu', 'ft', 'in', 'm'];

/* -------------------------------------------------------------------------- variants ---- */

/**
 * What a variant sku may be — the pod's own `@Pattern` on `PersistableProductVariant.sku`.
 *
 * Stricter than the product form's `SKU_PATTERN`: no dot. The suggestion logic maps a base sku's
 * dots to hyphens rather than proposing something the server would refuse.
 */
export const VARIANT_SKU_PATTERN = /^[A-Za-z0-9_-]+$/;

/** The pod's guardrails: at most this many assigned options and variants per product (422 beyond). */
export const MAX_VARIANT_OPTIONS = 4;
export const MAX_VARIANTS = 100;

/** One store option as the variants step offers it: the vocabulary entry, values in display order. */
export interface StoreOption {
  readonly id: number;
  readonly code: string;
  readonly name: string;
  readonly values: readonly StoreOptionValue[];
}

/** One value of a store option. The id is store-wide — it is what variants and facets key on. */
export interface StoreOptionValue {
  readonly id: number;
  readonly code: string;
  readonly name: string;
}

/**
 * One row of the variant matrix — a sellable combination with the fields the console edits.
 *
 * Catalog and inventory in one row, deliberately: the operator thinks "Red / L costs 30 and there
 * are 5", not "two services". `id` is the catalog variant row (kept so a save is not a re-create);
 * `price`/`quantity`/`available` are the inventory record for `sku`, merged in at load and written
 * back through the bulk upsert.
 */
export interface VariantMatrixRow {
  readonly id: number | null;
  readonly sku: string;
  /** One value id per assigned option, in axis order. */
  readonly optionValueIds: readonly number[];
  /** The value names, in axis order — the read-only cells of the row. */
  readonly labels: readonly string[];
  readonly isDefault: boolean;
  readonly price: number | null;
  readonly quantity: number;
  readonly available: boolean;
}

/** The canonical identity of a combination — sorted value ids, the pod's `option_signature`. */
export function combinationSignature(optionValueIds: readonly number[]): string {
  return [...optionValueIds].sort((left, right) => left - right).join('-');
}

/**
 * One product's images, as the Media step holds them.
 *
 * `isDefault` marks the storefront thumbnail, and it is the first image: `PUT …/product/{id}/images`
 * writes the order and the flag together, so reordering re-designates it. The old `PATCH
 * …/image/{imageId}` set `sortOrder` and nothing else, which is why this used to be read-only.
 */
export interface ProductImageItem {
  readonly id: number;
  /** The media library asset behind it, so the picker can show which ones are already attached. */
  readonly mediaAssetId: number | null;
  readonly name: string;
  readonly url: string | null;
  readonly altText: string | null;
  readonly order: number;
  readonly isDefault: boolean;
}

/** A product the form links to: a related product, shown with its SKU. */
export interface RelatedProduct {
  readonly id: number;
  readonly name: string;
  readonly sku: string;
}

/**
 * A product as the form reads it.
 *
 * `categoryIds` is the set the Organize step diffs against — the whole reason the step can apply
 * changes with `POST`/`DELETE …/product/{id}/category/{id}` instead of a whole-product write.
 */
export interface ProductDraft {
  readonly id: number | null;
  readonly sku: string;
  readonly visible: boolean;
  readonly canBePurchased: boolean;
  readonly shipeable: boolean;
  readonly virtual: boolean;
  /** `YYYY-MM-DD`, converted from the `Instant` the server sends. */
  readonly dateAvailable: string;
  readonly sortOrder: number;
  readonly price: number | null;
  readonly quantity: number;
  readonly weight: number | null;
  readonly height: number | null;
  readonly width: number | null;
  readonly length: number | null;
  readonly weightUnit: string;
  readonly dimensionUnit: string;
  readonly typeCode: string | null;
  readonly brandCode: string | null;
  readonly categoryIds: readonly number[];
  readonly copy: readonly LocalisedCopy[];
  readonly images: readonly ProductImageItem[];
  readonly related: readonly RelatedProduct[];
}

/** A new product, before anything has been typed into it. */
export function emptyDraft(languages: readonly string[]): ProductDraft {
  return {
    id: null,
    sku: '',
    // A new product is not on the storefront until the operator says so — Save draft depends on it.
    visible: false,
    canBePurchased: true,
    shipeable: true,
    virtual: false,
    dateAvailable: '',
    sortOrder: 0,
    price: null,
    quantity: 0,
    weight: null,
    height: null,
    width: null,
    length: null,
    weightUnit: 'kg',
    dimensionUnit: 'cm',
    typeCode: null,
    brandCode: null,
    categoryIds: [],
    copy: languages.map((language) => ({
      language,
      name: '',
      description: '',
      friendlyUrl: '',
      title: '',
      metaDescription: '',
      highlights: '',
      keyWords: '',
    })),
    images: [],
    related: [],
  };
}

/**
 * One line of the readiness checklist.
 *
 * Computed from the form itself, not fetched: every item is a field the console can already see, so
 * the panel costs no endpoint and cannot disagree with what is on screen.
 */
export interface ReadinessItem {
  readonly key: string;
  readonly done: boolean;
  /** A blocker for publishing, as against a recommendation. */
  readonly required: boolean;
}

/** One language's translation state, for the right-hand panel. */
export interface TranslationRow {
  readonly language: string;
  readonly name: string;
  /** How many of the seven copy fields carry something. */
  readonly filled: number;
  readonly total: number;
}

/** How many copy fields a translation row counts. Kept beside `TranslationRow` so the two agree. */
export const COPY_FIELD_COUNT = 5;
