import type {PageT} from '@core/table/table.types';
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
  readonly sku: string;
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

/** The wizard's four steps, in order. */
export type ProductStep = 'essentials' | 'media' | 'pricing' | 'organize';

export const PRODUCT_STEPS: readonly ProductStep[] = ['essentials', 'media', 'pricing', 'organize'];

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

/**
 * One product's images, as the Media step holds them.
 *
 * `isDefault` is read-only after the first upload: no endpoint re-designates a product's default
 * image — `PATCH …/image/{imageId}` sets `sortOrder` and nothing else. See lessons.md.
 */
export interface ProductImageItem {
  readonly id: number;
  readonly name: string;
  readonly url: string | null;
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
