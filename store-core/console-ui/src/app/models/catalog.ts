/**
 * The catalog pod's wire shapes — products, categories, brands, product types and product groups.
 *
 * Verified field by field against the Java DTOs under
 * `store-pod/catalog/catalog-commons/src/main/java/com/asrevo/cvhome/catalog/model/`.
 *
 * **Hardened against seller-core.** Every field there is optional, because `seller-ui`'s tsconfig
 * turns `strictNullChecks` off and the library inherits it. Under this app's `strict: true` that
 * would push a null check into every call site and hide the ones that matter. So the fields the
 * server always sends — `id`, and the collections its DTOs initialise to `new ArrayList<>()` — are
 * required here, and only what is genuinely absent stays optional. Each such narrowing is noted on
 * the field.
 *
 * **The initialiser is the test, and it is not uniform.** `ReadableCategory`, `ReadableProductGroup`
 * and `ReadableProductDefinition` declare their lists as `= new ArrayList<>()`, so they arrive as
 * `[]` and are required here. `ReadableManufacturer` and `ReadableProductType` declare theirs bare,
 * so they arrive as **`null`** — verified on the running stack, where a manufacturer answers
 * `"descriptions": null` while a category beside it answers `"descriptions": []`. Those two are
 * optional, and their mappers narrow. Assuming otherwise took the catalogue page down on first
 * load with "Cannot read properties of null".
 *
 * **What is deliberately not modelled.** `ReadableProductVariant`, `ReadableProductOption`,
 * `ReadableProductAttribute`, `ReadableProductProperty` and the price list. All are mapped on the
 * backend and none has ever had a client or a UI; the console builds one product with no variants.
 * That is a console gap, not a backend gap, so it is recorded in the module plan rather than in
 * `lessons.md`.
 */

/* ------------------------------------------------------------------ shared entity bases ---- */

/** `com.asrevo.cvhome.commons.domain.Entity`. The echo a `POST` answers with. */
export interface CreatedEntity {
  readonly id: number;
}

/** What every `…/unique?code=` endpoint answers. */
export interface EntityExists {
  readonly exists?: boolean;
}

/**
 * `store.core.model.catalog.NamedEntity` — one language's copy for anything nameable.
 *
 * `language` is a `LanguageCode` flattened to its code by `LanguageCodeSerializer`, so it arrives as
 * `"en"` and not `{"code":"en"}`. It is required: a description with no language cannot be matched
 * to a locale, and every populator sets it.
 */
export interface NamedDescription {
  readonly id?: number;
  readonly language: string;
  readonly name?: string;
  readonly description?: string;
  readonly friendlyUrl?: string;
  readonly keyWords?: string;
  readonly highlights?: string;
  readonly metaDescription?: string;
  readonly title?: string;
}

/* ------------------------------------------------------------------------- categories ---- */

/** `catalog-commons model/category/CategoryDescription`. */
export type CategoryDescription = NamedDescription;

/** `model/category/Category` — the shallow reference a parent link carries. */
export interface CategoryRef {
  readonly id?: number;
  readonly code?: string;
  readonly description?: CategoryDescription;
}

/**
 * `ReadableCategory` → `CategoryEntity` → `Category` → `Entity`.
 *
 * `id` and `code` are required: the entity cannot exist without either, and the tree keys on `id`.
 * `descriptions` and `children` are required because `ReadableCategory` initialises both to empty
 * lists — a category with no children arrives as `[]`, never as `null`.
 *
 * `parent` is genuinely absent on a top-level category, which is how the hierarchy is rooted.
 */
export interface ReadableCategory {
  readonly id: number;
  readonly code: string;
  readonly sortOrder?: number;
  readonly visible?: boolean;
  readonly featured?: boolean;
  /** Materialised path, e.g. `/1/7/`. Maintained by the pod; the console reads it, never writes it. */
  readonly lineage?: string;
  readonly depth?: number;
  readonly parent?: CategoryRef;
  /** Only populated when a single language was asked for; the private endpoints ask for all. */
  readonly description?: CategoryDescription;
  /** How many products sit in this category itself — not including its children. */
  readonly productCount?: number;
  readonly store?: string;
  readonly descriptions: readonly CategoryDescription[];
  readonly children: readonly ReadableCategory[];
}

/**
 * `PersistableCategory`, as `POST /private/category` and `PUT /private/category/{id}` take it —
 * and both echo it back.
 *
 * `descriptions` is the one field the facade always reads, so it is required on the way out.
 * `children` is deliberately never sent: the console re-parents with `PUT …/move/{parent}`, and a
 * nested body would ask the pod to rewrite a subtree it has not been given in full.
 */
export interface PersistableCategory {
  readonly id?: number;
  readonly code: string;
  readonly sortOrder?: number;
  readonly visible?: boolean;
  readonly featured?: boolean;
  readonly parent?: CategoryRef;
  readonly descriptions: readonly CategoryDescription[];
}

/** The paged envelope `ReadableCategoryList extends ReadableList<ReadableCategory>` serialises to. */

/* ----------------------------------------------------------------- brands / manufacturers ---- */

/** `model/manufacturer/ManufacturerDescription`. */
export type ManufacturerDescription = NamedDescription;

/**
 * `ReadableManufacturer` → `ManufacturerEntity` → `Manufacturer` → `Entity`.
 *
 * The whole record: an id, a code, a sort order and per-language copy. **No logo and no publish
 * flag** — the design draws both and neither exists. See lessons.md, "Catalogue — a category has no
 * banner image and a brand has no logo or publish flag".
 *
 * `descriptions` is **optional, and genuinely arrives as `null`**: the Java field is declared bare
 * where every sibling DTO declares `= new ArrayList<>()`. Confirmed against the running stack. See
 * the file header.
 */
export interface ReadableManufacturer {
  readonly id: number;
  readonly code: string;
  readonly order?: number;
  readonly description?: ManufacturerDescription;
  readonly descriptions?: readonly ManufacturerDescription[] | null;
}

/** `PersistableManufacturer`. `POST` echoes it back; `PUT` answers `void`. */
export interface PersistableManufacturer {
  readonly id?: number;
  readonly code: string;
  readonly order?: number;
  readonly descriptions: readonly ManufacturerDescription[];
}

/* ----------------------------------------------------------------------- product types ---- */

/** `model/product/type/ProductTypeDescription`. */
export type ProductTypeDescription = NamedDescription;

/**
 * `ReadableProductType` → `ProductTypeEntity` → `Entity`.
 *
 * **A type carries no attribute definitions.** The template's right-hand panel — attribute name,
 * kind, required, variant-defining — has nothing behind it: `ProductAttributeOptionApi` and
 * `ProductPropertySetApi` exist, but nothing links a *type* to the attributes a product of that
 * type must carry. See lessons.md, "Catalogue — a product type carries no attribute definitions".
 *
 * `descriptions` is optional for the same reason as `ReadableManufacturer`'s: the Java field carries
 * no initialiser. It happens to arrive as `[]` on the seeded store, which is exactly why it is worth
 * typing honestly rather than from one observation.
 */
export interface ReadableProductType {
  readonly id: number;
  readonly code: string;
  readonly allowAddToCart?: boolean;
  readonly visible?: boolean;
  readonly description?: ProductTypeDescription;
  readonly descriptions?: readonly ProductTypeDescription[] | null;
}

/** `PersistableProductType`. `POST` echoes only the new id; `PUT` answers `void`. */
export interface PersistableProductType {
  readonly id?: number;
  readonly code: string;
  readonly allowAddToCart?: boolean;
  readonly visible?: boolean;
  readonly descriptions: readonly ProductTypeDescription[];
}

/* ---------------------------------------------------------------------------- products ---- */

/** `model/product/ProductDescription`. */
export type ProductDescription = NamedDescription;

/**
 * `model/product/product/ProductSpecification` — the shipping box.
 *
 * `model` and `manufacturer` are free text on the specification and are **not** the product's brand;
 * the brand is `ReadableProduct.manufacturer`, a real entity. The console does not edit either of
 * the two free-text fields, but carries them so a save does not erase them.
 */
export interface ProductSpecification {
  readonly height?: number;
  readonly weight?: number;
  readonly length?: number;
  readonly width?: number;
  readonly model?: string;
  readonly manufacturer?: string;
  /** `MeasureUnit` — `CM` or `IN`. */
  readonly dimensionUnitOfMeasure?: string;
  /** `WeightUnit` — `KG` or `LB`. */
  readonly weightUnitOfMeasure?: string;
}

/** `model/product/ReadableImage` → `Entity`. */
export interface ReadableImage {
  readonly id: number;
  readonly imageName?: string;
  /** The pod's own path. Not necessarily a URL this browser can reach — see the Media step. */
  readonly imageUrl?: string;
  readonly externalUrl?: string;
  readonly videoUrl?: string;
  readonly imageType?: number;
  readonly order?: number;
  readonly defaultImage?: boolean;
}

/**
 * `ReadableProduct` → `ProductEntity` → `Product` → `Entity`, as the two list endpoints send it.
 *
 * Wider than seller-core typed it, and deliberately so: the list rows need a name, a category and a
 * brand, and `ReadableProductPopulator` fills all three, so asking a second endpoint per row would
 * be waste. `categories` and `images` are required because the DTO initialises them to empty lists.
 *
 * The variant, option, attribute and property lists the DTO also carries are not declared — see the
 * file header.
 */
export interface ReadableProduct {
  readonly id: number;
  readonly sku?: string;
  readonly available?: boolean;
  readonly visible?: boolean;
  readonly sortOrder?: number;
  readonly productShipeable?: boolean;
  readonly canBePurchased?: boolean;
  readonly dateAvailable?: string;
  /** The one language asked for, which for a console call is the console's active language. */
  readonly description?: ProductDescription;
  readonly image?: ReadableImage;
  readonly images?: readonly ReadableImage[];
  readonly manufacturer?: ReadableManufacturer;
  readonly type?: ReadableProductType;
  readonly categories?: readonly ReadableCategory[];
}

/**
 * `ReadableInventory` as it hangs off a product definition.
 *
 * **A single flat object, not a list** — and `price` reads back as a **string** while
 * `PersistableProductDefinition.price` writes a **number**. Both are typed as they actually are;
 * `product-form.api.service.ts` is the one place that converts.
 *
 * `productQuantityOrderMin`/`Max` are per-order *purchase limits* — the smallest and largest
 * quantity a shopper may put in one basket. They are not a reorder point and not a backorder flag,
 * which is why the design's low-stock blocks have nothing behind them. See lessons.md.
 */
export interface ReadableInventory {
  readonly id?: number;
  readonly sku?: string;
  readonly productId?: number;
  readonly price?: string;
  readonly quantity?: number;
  readonly available?: boolean;
  /** Free text on the entity. There is no location model behind it — see lessons.md. */
  readonly region?: string;
  readonly regionVariant?: string;
  readonly productQuantityOrderMin?: number;
  readonly productQuantityOrderMax?: number;
}

/**
 * `ReadableProductDefinition` → `ProductDefinition` → `Entity` — the v2 read the form is built on.
 *
 * Distinct from `ReadableProduct`: the definition is the *editable* product, and the two share only
 * their id. `descriptions`, `categories` and `images` are required because the DTO initialises all
 * three to empty lists.
 */
export interface ReadableProductDefinition {
  readonly id: number;
  readonly visible?: boolean;
  readonly shipeable?: boolean;
  readonly virtual?: boolean;
  /** `Instant`, so an ISO-8601 timestamp — not the `YYYY-MM-DD` the date input wants. */
  readonly dateAvailable?: string;
  readonly identifier?: string;
  readonly sku?: string;
  readonly productSpecifications?: ProductSpecification;
  readonly sortOrder?: number;
  readonly type?: ReadableProductType;
  readonly categories: readonly ReadableCategory[];
  readonly manufacturer?: ReadableManufacturer;
  readonly description?: ProductDescription;
  readonly images: readonly ReadableImage[];
  readonly descriptions: readonly ProductDescription[];
}

/**
 * `PersistableProductDefinition`, as `POST /api/v2/private/product` and
 * `PUT /api/v2/private/product/{id}` take it.
 *
 * `POST` echoes back an `Entity` — the new id and nothing else. `PUT` answers `void`.
 *
 * Two deviations from seller-core, both from reading the Java rather than the TypeScript:
 *
 * - `categories` is `List<Category>` on the DTO, not `unknown[]`. Typed as `CategoryRef[]`.
 * - `properties` is `List<PersistableProductAttribute>`, and the console writes none — the field is
 *   dropped rather than carried as an untyped array, because sending `[]` where the operator has
 *   attributes set elsewhere would clear them.
 *
 * `type` and `manufacturer` are the entities' **unique codes**, not their ids. This is the one place
 * in the catalog where a relation is addressed by code, and it is easy to get wrong: sending an id
 * here resolves to no manufacturer and silently drops the brand.
 */
export interface PersistableProductDefinition {
  readonly id?: number;
  readonly visible?: boolean;
  readonly shipeable?: boolean;
  readonly virtual?: boolean;
  readonly dateAvailable?: string;
  readonly identifier?: string;
  readonly sku: string;
  readonly productSpecifications?: ProductSpecification;
  readonly sortOrder?: number;
  readonly descriptions: readonly ProductDescription[];
  readonly categories?: readonly CategoryRef[];
  /** The product type's `code`. */
  readonly type?: string;
  /** The brand's `code`. */
  readonly manufacturer?: string;
}

/**
 * `LightPersistableProduct` — the body of the v1 `PATCH` the list's inline edit uses.
 *
 * Visibility only since the catalog/inventory split: price and quantity go to the inventory
 * service's sku-addressed upsert instead. Every field is a Java primitive, so a body that omits one
 * sets it to `false` rather than leaving it alone — the api service always sends both.
 */
export interface LightPersistableProduct {
  readonly available: boolean;
  readonly productShipeable: boolean;
}

/**
 * `PersistableInventory` — the inventory service's sku-addressed upsert body.
 *
 * `productId` is informational (the row keeps it so cleanup after a product delete can find it);
 * the SKU in the path is the key. The one price the single-product model uses is the default
 * `base` price.
 */
export interface PersistableInventory {
  readonly productId?: number;
  readonly quantity: number;
  readonly available: boolean;
  readonly prices: readonly {
    readonly code: string;
    readonly defaultPrice: boolean;
    readonly price: number;
  }[];
}

/**
 * `SkuInventory` — one SKU's stock and price from `GET /inventory/api/v1/availability`.
 *
 * `price` is the calculated final price; null when the SKU has no price configured yet. A SKU with
 * no inventory record at all is absent from the response, not present with zeros.
 */
export interface SkuInventory {
  readonly sku: string;
  readonly available: boolean;
  readonly canBePurchased: boolean;
  readonly quantity: number;
  readonly quantityOrderMinimum?: number;
  readonly quantityOrderMaximum?: number;
  readonly price?: {
    readonly finalPrice?: number;
    readonly originalPrice?: number;
    readonly discounted?: boolean;
    readonly stringPrice?: string;
  } | null;
}

/* ---------------------------------------------------------------------- product groups ---- */

/** `model/product/group/ReadableProductGroupDescription`. */
export type ProductGroupDescription = NamedDescription;

/**
 * `ReadableProductGroup` → `ProductGroup`.
 *
 * `products` is `List<ReadableProduct>` — whole products, not the `{id, sku}` pair seller-core
 * typed. That is what makes the members list renderable without a call per member.
 *
 * The same DTO answers `GET /products/{id}/relationship`, where the "group" is the set of products
 * related to one product. One shape, two meanings, and the endpoint is what tells them apart.
 */
export interface ReadableProductGroup {
  readonly id?: number;
  readonly code?: string;
  readonly active?: boolean;
  readonly description?: ProductGroupDescription;
  readonly descriptions: readonly ProductGroupDescription[];
  readonly parentProduct?: ReadableProduct;
  readonly products: readonly ReadableProduct[];
}

/**
 * `PersistableProductGroup`, as `POST /private/products/groups` takes it.
 *
 * There is no `PUT`: the `POST` is an upsert keyed on `code`, which is why editing a group means
 * re-posting it whole. A body that omits `descriptions` clears them.
 */
export interface PersistableProductGroup {
  readonly id?: number;
  readonly code: string;
  readonly active?: boolean;
  readonly descriptions: readonly ProductGroupDescription[];
  readonly parentProductId?: number;
  readonly productIds?: readonly number[];
}
