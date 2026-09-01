import {Description} from "./description";
import {Category} from "./category";

/** Page of products as the catalog returns it (`/api/v2/products`): `pageNumber`/`size`/`totalElements` are
 *  the live fields; `number`/`recordsTotal` are kept for older payloads. */
export interface ProductGroupPage {
    totalPages: number
    number?: number
    pageNumber?: number
    size?: number
    totalElements?: number
    recordsTotal?: number
    recordsFiltered: number
    content: Product[] | undefined
    productGroup: ProductGroup | undefined
}

export interface ProductGroup {
    description: Description | undefined
    products: Product[] | undefined
    code: string
    active: boolean
    id: number
}

export interface Product {
    id: number
    productShipeable: boolean
    available: boolean
    visible: boolean
    sortOrder: number
    dateAvailable: string
    creationDate: string
    price: number
    quantity: number
    sku: string
    preOrder: boolean
    productVirtual: boolean
    quantityOrderMaximum: number
    quantityOrderMinimum: number
    productIsFree: boolean
    productSpecifications: ProductSpecifications | undefined
    rating: number
    ratingCount: number
    refSku: string
    rentalDuration: number
    rentalPeriod: number
    description: Description | undefined
    productPrice: ProductPrice | undefined
    finalPrice: string
    originalPrice: string
    discounted: boolean
    image: Image | undefined
    images: Image[] | undefined
    manufacturer: Manufacturer | undefined
    /** Dead on the wire — see `ProductAttribute`. */
    attributes: ProductAttribute[] | undefined
    /** The axes this product varies by. Filled on the PDP read; listings leave it empty. */
    options: ProductOption[] | undefined
    /** Every sellable combination (≥1). Filled on the PDP read; listings leave it empty. */
    variants: ProductVariant[] | undefined
    /**
     * How many variants the product owns — the one variant fact a listing card gets. `> 1` means
     * "has options"; nothing stores a flag, and cards never load the variant rows themselves.
     */
    variantCount?: number
    categories: Category[] | undefined
    type: any
    canBePurchased: boolean
    owner: any
    subTotal: number
    displaySubTotal: string
    /** On a cart/order line read by combination sku: the selected option labels. */
    variant?: VariantSelection
}

export interface ProductSpecifications {
    height: any
    weight: any
    length: any
    width: any
    model: any
    manufacturer: any
    dimensionUnitOfMeasure: any
    weightUnitOfMeasure: any
}


export interface ProductPrice {
    id: number
    originalPrice: string
    finalPrice: string
    discounted: boolean
    description: Description
}

export interface Image {
    id: number
    /** The content media asset behind this image; null for an external url or a video.
     *  Optional because nothing in the storefront reads it — `toListingProduct` drops it from list payloads. */
    mediaAssetId?: number | null
    imageUrl: string
    /** Overrides the asset's own alt text for this product. */
    altText: string | null
    /** Unread by the storefront; see `mediaAssetId`. */
    externalUrl?: any
    /** Unread by the storefront; see `mediaAssetId`. */
    videoUrl?: any
    /** Unread by the storefront; see `mediaAssetId`. */
    imageType?: number
    order: number
    defaultImage: boolean
}


export interface Manufacturer {
    id: number
    code: string
    order: number
    description: Description
}

export interface Parent {
    id: number
    code: string
}

/** A product group code the home page renders. */
export type ProductGroupCode = 'FEATURED_ITEMS' | 'NEWLY_ADDED' | 'RECOMMENDED';

/* ---- options / variants / attributes (mirror catalog ReadableProductOption & co.) ---------------- */

/**
 * One axis this product varies by — Color, Size. Store-wide vocabulary, assigned per product; the
 * PDP payload carries the assigned axes in display order, each holding **only the values its
 * variants actually use** (no dead chips). Every option here is variant-defining — the old
 * `variant` flag is gone with the old model.
 */
export interface ProductOption {
    id: number
    code: string
    name: string
    sortOrder?: number
    /**
     * The wire field is `values`, not `optionValues` — `ReadableProductOption.values`. The
     * deprecated DTO this replaced used `optionValues`, and carrying that name over silently
     * emptied every chip rail: the option arrived, its values did not, so the PDP rendered a
     * product with three variants and no way to pick one.
     */
    values: ProductOptionValue[]
}

/** One value of an option. Ids are store-wide — the same ids `ListingQuery.optionValueIds` sends. */
export interface ProductOptionValue {
    id: number
    code: string
    name: string
    sortOrder?: number
}

/**
 * One sellable combination — its own sku, one value per assigned option. Every product owns ≥1
 * variant under the uniform model; a product with no options owns exactly one default variant and
 * the PDP resolves it without a selection.
 *
 * Price and stock are the inventory service's, merged in by `InventoryService.enrichProduct` — the
 * `VariantPricing` section below is written there and nowhere else.
 */
export interface ProductVariant extends VariantPricing {
    id: number
    sku: string
    defaultVariant: boolean
    sortOrder?: number
    /** One value id per assigned option — match against a selection by set equality. */
    optionValueIds: number[]
}

/** What enrichment attaches to a variant. Absent until `enrichProduct` has run for the PDP. */
export interface VariantPricing {
    quantity?: number
    canBePurchased?: boolean
    /** The merchant's per-order floor and ceiling for this sku; `0` maximum means no limit. */
    quantityOrderMinimum?: number
    quantityOrderMaximum?: number
    /** Pre-formatted in the store currency, like the product-level pair. */
    finalPrice?: string
    originalPrice?: string
    discounted?: boolean
}

/**
 * The variant a sku-addressed read resolved to — filled on cart and order lines, where it renders
 * as "Color: Red / Size: L". Absent on a default variant and everywhere a read is not by sku.
 */
export interface VariantSelection {
    sku: string
    optionValues: VariantSelectionValue[]
}

export interface VariantSelectionValue {
    optionId: number
    optionCode: string
    optionName?: string
    valueId: number
    valueCode: string
    valueName?: string
    sortOrder?: number
}

/**
 * Descriptive attribute ("Material: cotton"); never affects the SKU.
 *
 * **Dead on the wire.** The rewritten catalog sends no `attributes` — non-variant descriptive
 * attributes are a future feature, deliberately not smuggled into the variant model. The shape
 * stays because every theme's product page renders a specifications block from it (degrading to
 * nothing today); delete it together with those blocks or revive it when the feature lands.
 */
export interface ProductAttribute {
    id: number
    code: string
    name: string
    lang: string | undefined
    type: string | undefined
    attributeValues: ProductAttributeValue[]
}

export interface ProductAttributeValue {
    id: number
    code: string
    name: string | undefined
    lang: string | undefined
    description: string | undefined
}
