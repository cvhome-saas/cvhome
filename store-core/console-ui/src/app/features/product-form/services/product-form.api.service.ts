import {Injectable, inject} from '@angular/core';
import {Observable, catchError, concat, forkJoin, last, map, of, switchMap, toArray} from 'rxjs';

import {CatalogReference} from '@api/catalog/catalog-reference.service';
import {CategoryService} from '@api/catalog/category.service';
import {ProductImageService} from '@api/catalog/product-image.service';
import {ProductOptionService} from '@api/catalog/product-option.service';
import {ProductRelationshipService} from '@api/catalog/product-relationship.service';
import {ProductService} from '@api/catalog/product.service';
import {ProductVariantService} from '@api/catalog/product-variant.service';
import {InventoryService} from '@api/inventory/inventory.service';
import type {
  PersistableProductDefinition,
  PersistableProductImage,
  PersistableSkuInventory,
  PersistableVariantSet,
  ProductDescription,
  ReadableCategory,
  ReadableProductDefinition,
  ReadableProductOption,
  ReadableProductVariantDefinition,
  SkuInventory,
} from '@models/catalog';
import type {
  ProductDraft,
  ProductImageItem,
  RelatedProduct,
  StoreOption,
  VariantMatrixRow,
} from '@models/products';
import {emptyDraft} from '@models/products';
import type {LocalisedCopy} from '@models/taxonomy';

/**
 * What a create ended up doing.
 *
 * Two outcomes rather than one id, because the product and its categories are separate writes and
 * the second can fail after the first has landed. Reporting that as a failed create is how an
 * operator ends up with a duplicate SKU error for a product they were told did not exist.
 */
export interface CreateOutcome {
  readonly id: number;
  readonly categoriesApplied: boolean;
  /** Whether the price/quantity write to the inventory service landed. */
  readonly inventoryApplied: boolean;
}

/** The same distinction for a save: the definition landed, the category diff or stock write may not have. */
export interface UpdateOutcome {
  readonly snapshot: ProductFormSnapshot;
  readonly categoriesApplied: boolean;
  readonly inventoryApplied: boolean;
}

/** Everything the form needs to render, whether the product exists yet or not. */
export interface ProductFormSnapshot {
  readonly draft: ProductDraft;
  readonly categories: readonly CategoryOption[];
  readonly brands: readonly BrandOption[];
  readonly types: readonly ProductTypeOption[];
  readonly languages: readonly string[];
  readonly currency: string | null;
  /** The store's option vocabulary, for the variants step's axis picker. Empty when unreadable. */
  readonly vocabulary: readonly StoreOption[];
  /** The options this product varies by, in display order — ids into `vocabulary`. */
  readonly assignedOptionIds: readonly number[];
  /** The combination variants, price/stock merged in. Empty for a simple (default-variant) product. */
  readonly variants: readonly VariantMatrixRow[];
  /**
   * The variant read failed, so `variants` is unknown rather than empty.
   *
   * The two used to collapse into `[]`, which told the step "this product sells as one SKU" for a
   * product that may own a dozen combinations — and the step's whole-set replace would then have
   * destroyed them. The step renders the difference and refuses to save while it is true.
   */
  readonly variantsUnavailable: boolean;
  /** Likewise for the axis picker: no options read is not the same as the store having none. */
  readonly vocabularyUnavailable: boolean;
}

/** What a variant-set save ended up doing — the same honesty split as `CreateOutcome`. */
export interface VariantSaveOutcome {
  /** The catalog write landed (it is atomic — false never reaches the caller, a failure throws). */
  readonly variantsApplied: true;
  /** Whether every price/stock upsert and retired-sku cleanup landed. */
  readonly inventoryApplied: boolean;
  /**
   * Exactly what the inventory leg was trying to write when it did not land, or null when it did.
   *
   * The retry replays *this*, not whatever the matrix holds by then. It used to re-send
   * `variantRows()` with no removed skus, and since the facade reloaded the snapshot on the
   * failure branch those rows had already reverted to the server's — so the retry wrote the old
   * prices back and left every retired sku's inventory row orphaned for good.
   */
  readonly pendingInventory: PendingVariantInventory | null;
}

/** The inventory half of a variant save, captured so a retry replays the same intent. */
export interface PendingVariantInventory {
  readonly rows: readonly VariantMatrixRow[];
  readonly removedSkus: readonly string[];
}

/** A category as the Organize step's picker lists it: flat, with its depth kept for the indent. */
export interface CategoryOption {
  readonly id: number;
  readonly label: string;
  readonly depth: number;
}

/**
 * A brand as the Organize step's select offers it.
 *
 * The **code**, not the id. `PersistableProductDefinition.manufacturer` is a unique code, which is
 * the one place on this platform a relation is addressed that way — carrying the id here and
 * sending it would silently resolve to no brand at all.
 */
export interface BrandOption {
  readonly code: string;
  readonly label: string;
}

export interface ProductTypeOption {
  readonly code: string;
  readonly label: string;
}

/**
 * The product form's reads and writes.
 *
 * **Categories are applied by diffing, not by the product write.** `PersistableProductDefinition`
 * carries a `categories` list, but the endpoints that actually maintain the join are
 * `POST`/`DELETE …/product/{productId}/category/{categoryId}` — and those are what the console
 * calls, comparing the chosen set against what the definition returned and issuing one call per
 * difference. This is only possible because the port fixed seller-core's stray trailing brace on
 * the `POST` path, which meant that endpoint had never once been reached from the old console.
 *
 * **Images and relationships need a saved product.** Both are addressed by product id, so on
 * `/products/new` there is nothing to attach them to. Save draft — a `POST` with `visible: false` —
 * creates the product and the page moves to `/products/:id`, where every step is live.
 */
@Injectable({providedIn: 'root'})
export class ProductFormApi {
  private readonly products = inject(ProductService);
  private readonly variants = inject(ProductVariantService);
  private readonly productOptions = inject(ProductOptionService);
  private readonly inventory = inject(InventoryService);
  private readonly images = inject(ProductImageService);
  private readonly relationships = inject(ProductRelationshipService);
  private readonly categories = inject(CategoryService);
  private readonly reference = inject(CatalogReference);

  /** The definition the server last sent, for fields the form does not edit but must not clear. */
  private loaded: ReadableProductDefinition | null = null;

  /** Every variant sku the server last sent — what a variant save diffs against to retire rows. */
  private loadedVariantSkus: readonly string[] = [];

  /**
   * A product, or a blank one.
   *
   * The three reference lists are optional in the usual sense — a brand list that fails costs the
   * Organize step its brand select, not the whole form. The product itself is not: a form bound to
   * nothing is not a form.
   */
  load(productId: number | null): Observable<ProductFormSnapshot> {
    return forkJoin({
      definition: productId === null ? of(null) : this.products.definition(productId),
      /*
       * The product's variant set — the matrix rows and, implied by them, the axes. Optional: a
       * form without its matrix still edits copy and images, and the step says what it could not
       * load. `null` (leg failed) and `[]` cannot happen apart — every saved product owns ≥1
       * variant — so `null` is the only "unknown" the step has to render.
       */
      variantRows:
        productId === null
          ? of<readonly ReadableProductVariantDefinition[] | null>(null)
          : this.optional(this.variants.list(productId)),
      /*
       * The store's option vocabulary, for the axis picker. Read directly rather than through the
       * reference cache: options carry their whole value lists, which the other cached lists do
       * not need, and the catalogue invalidates the cache on every option write anyway.
       */
      vocabulary: this.optional(this.productOptions.list({page: 0, count: 200})),
      related: productId === null ? of(null) : this.optional(this.relationships.related(productId)),
      /*
       * Through the shared reference cache, not straight to the endpoints. These four are the same
       * four the catalogue reads, they change only when someone edits the catalogue, and moving
       * between the products list and a product used to re-read all of them every time. The
       * catalogue invalidates the cache after every write, so an edit is still visible immediately.
       */
      categories: this.optional(this.reference.hierarchy()),
      brands: this.optional(this.reference.brandList()),
      types: this.optional(this.reference.typeList()),
      store: this.optional(this.reference.store()),
    }).pipe(
      /*
       * Price, quantity and purchasability come from the inventory service since the split — ONE
       * bulk read for the default variant's sku and every combination sku together, chained after
       * the catalog reads because the sku list comes from them. Optional in the same sense as the
       * reference lists: a form without stock numbers beats no form.
       */
      switchMap((loaded) => {
        const skus = new Set<string>();
        if (loaded.definition?.sku) {
          skus.add(loaded.definition.sku);
        }
        for (const row of loaded.variantRows ?? []) {
          skus.add(row.sku);
        }
        const stock: Observable<readonly SkuInventory[] | null> = skus.size
          ? this.optional(this.inventory.bySkus([...skus]))
          : of([]);
        return stock.pipe(map((inventories) => ({...loaded, inventories: inventories ?? []})));
      }),
      map(({definition, variantRows, vocabulary, related, categories, brands, types, store, inventories}) => {
        const bySku = new Map(inventories.map((inventory) => [inventory.sku, inventory]));
        const stock = definition?.sku ? (bySku.get(definition.sku) ?? null) : null;
        this.loaded = definition;
        this.loadedVariantSkus = (variantRows ?? []).map((row) => row.sku);
        const languages = store?.supportedLanguages?.length
          ? [...store.supportedLanguages]
          : [store?.defaultLanguage ?? 'en'];

        const combinationRows = (variantRows ?? []).filter((row) => row.optionValues.length > 0);
        return {
          draft:
            definition === null
              ? emptyDraft(languages)
              : toDraft(
                  definition,
                  stock,
                  languages,
                  (related?.products ?? []).map(
                    (product): RelatedProduct => ({
                      id: product.id,
                      name: product.description?.name ?? product.sku ?? String(product.id),
                      sku: product.sku ?? '',
                    }),
                  ),
                ),
          vocabulary: (vocabulary?.content ?? []).map(toStoreOption),
          assignedOptionIds: assignedOptions(combinationRows),
          variants: combinationRows.map((row) => toMatrixRow(row, bySku)),
          // A new product has no variants to read; anything else answering null is a failed leg.
          variantsUnavailable: productId !== null && variantRows === null,
          vocabularyUnavailable: vocabulary === null,
          categories: flattenCategories(categories?.content ?? [], 0),
          brands: (brands?.content ?? []).map((brand) => ({
            code: brand.code,
            // Null on the wire for both of these — see `@models/catalog`.
            label: brand.description?.name ?? brand.descriptions?.[0]?.name ?? brand.code,
          })),
          types: (types?.content ?? []).map((type) => ({
            code: type.code,
            label: type.description?.name ?? type.descriptions?.[0]?.name ?? type.code,
          })),
          languages,
          currency: store?.currency ?? null,
        };
      }),
    );
  }

  /**
   * Create the product, and answer its new id.
   *
   * The `POST` echoes an `Entity` and nothing else, which is exactly what the page routes on. The
   * category set is applied straight afterwards, because a brand new product is in no categories
   * and every chosen one is an addition.
   */
  create(draft: ProductDraft): Observable<CreateOutcome> {
    return this.products.create(toPersistable(draft)).pipe(
      switchMap((created) =>
        this.applyCategories(created.id, [], draft.categoryIds).pipe(
          map(() => ({id: created.id, categoriesApplied: true})),
          /*
           * **The product exists.** Letting the category leg fail the whole stream reported "could
           * not save" for a product that had just been created — and the operator's next Save draft
           * then failed on a duplicate SKU. The id is the success; the categories are a warning the
           * Organize step will show the truth about as soon as it reloads.
           */
          catchError(() => of({id: created.id, categoriesApplied: false})),
        ),
      ),
      /*
       * Stock and price are the inventory service's since the split, so a create is two writes.
       * Same stance as the categories: the product exists, so a failed stock write is reported as
       * a warning, not as a failed create.
       */
      switchMap((outcome) =>
        this.applyInventory(draft, outcome.id).pipe(
          map((inventoryApplied) => ({...outcome, inventoryApplied})),
        ),
      ),
    );
  }

  /**
   * Save an existing product.
   *
   * Two writes, in order: the definition, then the category diff. `PUT` answers `void`, so the
   * caller reloads rather than assuming — the pod slugifies, trims and defaults enough of this DTO
   * that echoing the request back would be showing the operator their own typing.
   */
  update(productId: number, draft: ProductDraft, writeInventory = true): Observable<UpdateOutcome> {
    const before = (this.loaded?.categories ?? []).map((category) => category.id);
    return this.products.update(productId, toPersistable(draft)).pipe(
      switchMap(() =>
        this.applyCategories(productId, before, draft.categoryIds).pipe(
          map(() => true),
          // The definition landed. Failing the stream here would leave the form showing the
          // operator's input as though nothing had been saved, when most of it had.
          catchError(() => of(false)),
        ),
      ),
      /*
       * `writeInventory: false` is the multi-variant product: price and stock live one row per
       * combination sku and are written by the variants step's own save — the single-sku upsert
       * here would race it over the default variant's row for values the form no longer edits.
       */
      switchMap((categoriesApplied) =>
        (writeInventory ? this.applyInventory(draft, productId) : of(true)).pipe(
          map((inventoryApplied) => ({categoriesApplied, inventoryApplied})),
        ),
      ),
      switchMap(({categoriesApplied, inventoryApplied}) =>
        this.load(productId).pipe(
          map((snapshot) => ({snapshot, categoriesApplied, inventoryApplied})),
        ),
      ),
    );
  }

  /**
   * The stock-and-price half of a save: the sku-addressed upsert in the inventory service.
   *
   * `canBePurchased` maps onto the inventory record's `available` flag — the catalog's own
   * `visible` stays the merchandising switch. Best-effort like the category diff, and for the same
   * reason: the definition already landed.
   */
  private applyInventory(draft: ProductDraft, productId: number): Observable<boolean> {
    if (!draft.sku) {
      return of(false);
    }
    return this.inventory
      .upsert(draft.sku, {
        productId,
        quantity: draft.quantity,
        available: draft.canBePurchased,
        price: {amount: draft.price ?? 0},
      })
      .pipe(
        map(() => true),
        catchError(() => of(false)),
      );
  }

  /**
   * Bring the product's categories to the chosen set.
   *
   * A diff rather than a replace: there is no "set the categories" endpoint, only add and remove,
   * and sending every chosen category as an add would 409 on the ones already there.
   *
   * `concat` rather than `forkJoin`: these run against one row on one product and firing eight
   * writes at once is how a join table gets a duplicate. Empty diffs answer immediately.
   */
  private applyCategories(
    productId: number,
    before: readonly number[],
    after: readonly number[],
  ): Observable<unknown> {
    const added = after.filter((id) => !before.includes(id));
    const removed = before.filter((id) => !after.includes(id));
    const calls = [
      ...added.map((id) => this.products.addToCategory(productId, id)),
      ...removed.map((id) => this.products.removeFromCategory(productId, id)),
    ];
    return calls.length ? concat(...calls).pipe(toArray()) : of(null);
  }

  /* -------------------------------------------------------------------- variants ---- */

  /**
   * Replace the product's variant set, then bring inventory to match — the explicit orchestration
   * the module plan demands, with **no silent legs**.
   *
   * The catalog `PUT` is atomic (axes and combinations together) and its failure fails the whole
   * call: nothing else has run, the operator retries the save. Once it lands, the inventory work —
   * one bulk upsert for every priced row plus one delete per retired sku — is a fact the outcome
   * reports honestly: `inventoryApplied: false` means the catalog now says one thing and inventory
   * another, and the facade shows a *retryable* error state rather than a silent half-save. Both
   * legs are idempotent (`PUT` upsert; delete of a missing row is a no-op), so a retry is safe.
   */
  saveVariants(
    productId: number,
    set: PersistableVariantSet,
    rows: readonly VariantMatrixRow[],
  ): Observable<VariantSaveOutcome> {
    return this.variants.replace(productId, set).pipe(
      /*
       * Which skus to retire is diffed against what the product owns AFTER the write, not against
       * the request: an empty set restores a default variant whose sku the service *keeps* from
       * the retiring first row, and diffing against the request would delete that sku's inventory
       * row — the restored product would lose its price. One extra GET buys the truth.
       */
      switchMap(() => this.optional(this.variants.list(productId))),
      switchMap((after) => {
        // If the re-read failed, retire nothing — a stale inventory row is recoverable, a deleted
        // price is a support ticket.
        const keep = new Set((after ?? []).map((variant) => variant.sku));
        const removed =
          after === null ? [] : this.loadedVariantSkus.filter((sku) => !keep.has(sku));
        return this.applyVariantInventory(productId, rows, removed).pipe(
          map((inventoryApplied) => ({
            variantsApplied: true as const,
            inventoryApplied,
            pendingInventory: inventoryApplied ? null : {rows, removedSkus: removed},
          })),
        );
      }),
    );
  }

  /**
   * The inventory half of a variant save, callable on its own — this is the retry the facade
   * offers when the catalog write landed and the inventory one did not.
   */
  applyVariantInventory(
    productId: number,
    rows: readonly VariantMatrixRow[],
    removedSkus: readonly string[],
  ): Observable<boolean> {
    const entries: PersistableSkuInventory[] = rows
      // A row without a price has no inventory record to write yet — the readiness checklist is
      // what tells the operator, and the publish button stays disabled until it is priced.
      .filter((row) => row.price !== null)
      .map((row) => ({
        sku: row.sku,
        inventory: {
          productId,
          quantity: row.quantity,
          available: row.available,
          price: {amount: row.price ?? 0},
        },
      }));
    const writes: Observable<unknown>[] = [
      ...(entries.length ? [this.inventory.bulkUpsert(entries)] : []),
      ...removedSkus.map((sku) => this.inventory.deleteBySku(sku)),
    ];
    if (writes.length === 0) {
      return of(true);
    }
    return concat(...writes).pipe(
      toArray(),
      map(() => true),
      // Reported, not swallowed: the caller renders a retry, which is the opposite of best-effort.
      catchError(() => of(false)),
    );
  }

  /* ---------------------------------------------------------------------- images ---- */

  /**
   * Attaches library assets and answers the product's whole gallery afterwards.
   *
   * One request, not one per image: the pod appends the batch, assigns the order and decides the
   * default itself. The old upload path had to send files one at a time because two racing uploads
   * would both see "no default yet" and the product would end up with two.
   */
  attachImages(
    productId: number,
    assets: readonly PersistableProductImage[],
  ): Observable<readonly ProductImageItem[]> {
    return this.settling(productId, this.images.attach(productId, assets));
  }

  removeImage(productId: number, imageId: number): Observable<readonly ProductImageItem[]> {
    return this.settling(productId, this.images.remove(productId, imageId));
  }

  /**
   * Runs a set of image writes and then answers what the product's gallery actually holds.
   *
   * Always ends with a read rather than assuming the writes took: these are batches, and the pod
   * decides `sortOrder` and the default flag itself. A batch that fails partway propagates the
   * failure — recovering the gallery is the caller's job, because only the caller has somewhere to
   * put it. `ProductFormFacade.refreshGallery` is that recovery.
   */
  private settling(
    productId: number,
    writes: Observable<unknown>,
  ): Observable<readonly ProductImageItem[]> {
    return (concat(writes, this.gallery(productId)) as Observable<readonly ProductImageItem[]>).pipe(
      last(),
    );
  }

  /**
   * Replaces the gallery: order is the list order, and the item flagged default wins.
   *
   * One request for the whole list. Reordering used to be one `PATCH` per image, which left two
   * images sharing a position whenever a call in the middle failed — and could not set the default
   * at all.
   */
  replaceImages(
    productId: number,
    ordered: readonly ProductImageItem[],
  ): Observable<readonly ProductImageItem[]> {
    const body: PersistableProductImage[] = ordered.map((image) => ({
      mediaAssetId: image.mediaAssetId,
      externalUrl: image.mediaAssetId === null ? image.url : null,
      altText: image.altText,
      defaultImage: image.isDefault,
    }));
    return this.settling(productId, this.images.replace(productId, body));
  }

  private gallery(productId: number): Observable<readonly ProductImageItem[]> {
    return this.images.images(productId).pipe(
      map((images) =>
        [...images]
          .sort((left, right) => (left.order ?? 0) - (right.order ?? 0))
          .map((image) => ({
            id: image.id,
            mediaAssetId: image.mediaAssetId ?? null,
            name: image.altText ?? '',
            url: image.imageUrl ?? image.externalUrl ?? null,
            altText: image.altText ?? null,
            order: image.order ?? 0,
            isDefault: image.defaultImage ?? false,
          })),
      ),
    );
  }

  /* ----------------------------------------------------------------- relationships ---- */

  addRelated(productId: number, relatedId: number): Observable<readonly RelatedProduct[]> {
    return this.relationships
      .add(productId, relatedId)
      .pipe(switchMap(() => this.relatedFor(productId)));
  }

  removeRelated(productId: number, relatedId: number): Observable<readonly RelatedProduct[]> {
    return this.relationships
      .remove(productId, relatedId)
      .pipe(switchMap(() => this.relatedFor(productId)));
  }

  private relatedFor(productId: number): Observable<readonly RelatedProduct[]> {
    return this.relationships.related(productId).pipe(
      map((group) =>
        group.products.map((product) => ({
          id: product.id,
          name: product.description?.name ?? product.sku ?? String(product.id),
          sku: product.sku ?? '',
        })),
      ),
      catchError(() => of<readonly RelatedProduct[]>([])),
    );
  }

  /**
   * Products matching a SKU fragment, for the related-products picker.
   *
   * By SKU, not by name — see `ProductService.search`. The product being edited is filtered out
   * here rather than server-side: nothing relates a product to itself, and offering it would be an
   * invitation to a 400.
   */
  searchProducts(term: string, excludeId: number | null): Observable<readonly RelatedProduct[]> {
    return this.products.search({page: 0, count: 10, sku: term}).pipe(
      map((page) =>
        page.content
          .filter((product) => product.id !== excludeId)
          .map((product) => ({
            id: product.id,
            name: product.description?.name ?? product.sku ?? String(product.id),
            sku: product.sku ?? '',
          })),
      ),
      catchError(() => of<readonly RelatedProduct[]>([])),
    );
  }

  /** Whether a SKU is taken. Live, as the field is typed — the only uniqueness answer available. */
  skuTaken(sku: string): Observable<boolean> {
    return this.products.skuTaken(sku).pipe(
      map((answer) => answer.exists === true),
      // A check that could not be made is not a check that failed: the field stays usable and the
      // server has the final word on the save.
      catchError(() => of(false)),
    );
  }

  private optional<T>(source: Observable<T>): Observable<T | null> {
    return source.pipe(catchError(() => of(null)));
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

function toDraft(
  definition: ReadableProductDefinition,
  stock: SkuInventory | null,
  languages: readonly string[],
  related: readonly RelatedProduct[],
): ProductDraft {
  const specification = definition.productSpecifications;

  return {
    id: definition.id,
    sku: definition.sku ?? '',
    visible: definition.visible ?? false,
    // The inventory record's `available` flag; a product that has never been stocked can be bought
    // once it is, so the blank default stays `true`.
    canBePurchased: stock?.available ?? true,
    shipeable: definition.shipeable ?? true,
    virtual: definition.virtual ?? false,
    // `Instant` on the wire; `<input type="date">` wants `YYYY-MM-DD`.
    dateAvailable: (definition.dateAvailable ?? '').slice(0, 10),
    sortOrder: definition.sortOrder ?? 0,
    price: stock?.price?.originalPrice ?? stock?.price?.finalPrice ?? null,
    quantity: stock?.quantity ?? 0,
    weight: specification?.weight ?? null,
    height: specification?.height ?? null,
    width: specification?.width ?? null,
    length: specification?.length ?? null,
    // Lowercase, matching the server's enum. See `WEIGHT_UNITS`.
    weightUnit: specification?.weightUnitOfMeasure || 'kg',
    dimensionUnit: specification?.dimensionUnitOfMeasure || 'cm',
    typeCode: definition.type?.code ?? null,
    brandCode: definition.manufacturer?.code ?? null,
    categoryIds: definition.categories.map((category) => category.id),
    copy: mergeCopy(definition.descriptions, languages),
    images: [...definition.images]
      .sort((left, right) => (left.order ?? 0) - (right.order ?? 0))
      .map((image) => ({
        id: image.id,
        mediaAssetId: image.mediaAssetId ?? null,
        name: image.altText ?? '',
        url: image.imageUrl ?? image.externalUrl ?? null,
        altText: image.altText ?? null,
        order: image.order ?? 0,
        isDefault: image.defaultImage ?? false,
      })),
    related,
  };
}

/**
 * The product's copy, one entry per language the store trades in.
 *
 * A language the product has never been written in still gets a row, because the translations panel
 * has to be able to say it is missing — and because the operator has to have somewhere to type it.
 * A language the product has copy in but the store no longer lists is kept too: dropping it would
 * silently delete that copy on the next save.
 */
function mergeCopy(
  descriptions: readonly ProductDescription[],
  languages: readonly string[],
): readonly LocalisedCopy[] {
  const byLanguage = new Map<string, LocalisedCopy>();
  for (const language of languages) {
    byLanguage.set(language, blank(language));
  }
  for (const description of descriptions) {
    byLanguage.set(description.language, {
      language: description.language,
      name: description.name ?? '',
      description: description.description ?? '',
      friendlyUrl: description.friendlyUrl ?? '',
      title: description.title ?? '',
      metaDescription: description.metaDescription ?? '',
      highlights: description.highlights ?? '',
      keyWords: description.keyWords ?? '',
    });
  }
  return [...byLanguage.values()];
}

function blank(language: string): LocalisedCopy {
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
 * The draft as the v2 endpoints take it.
 *
 * Two details worth stating. `type` and `manufacturer` are the entities' **codes**, not their ids —
 * the one place on this platform where a relation is addressed by code, and sending an id here
 * resolves to nothing and silently drops the brand. And `dateAvailable` is an `Instant`, so a bare
 * `YYYY-MM-DD` does not parse; it is widened to midnight UTC, and omitted entirely when empty
 * rather than sent as `''`.
 *
 * `categories` is deliberately not sent even though the DTO carries it: the join is maintained by
 * the two dedicated endpoints, which is what `applyCategories` calls.
 */
function toPersistable(draft: ProductDraft): PersistableProductDefinition {
  return {
    ...(draft.id !== null ? {id: draft.id} : {}),
    sku: draft.sku,
    visible: draft.visible,
    shipeable: draft.shipeable,
    virtual: draft.virtual,
    sortOrder: draft.sortOrder,
    ...(draft.dateAvailable ? {dateAvailable: `${draft.dateAvailable}T00:00:00Z`} : {}),
    productSpecifications: {
      ...(draft.weight !== null ? {weight: draft.weight} : {}),
      ...(draft.height !== null ? {height: draft.height} : {}),
      ...(draft.width !== null ? {width: draft.width} : {}),
      ...(draft.length !== null ? {length: draft.length} : {}),
      weightUnitOfMeasure: draft.weightUnit,
      dimensionUnitOfMeasure: draft.dimensionUnit,
    },
    ...(draft.typeCode ? {type: draft.typeCode} : {}),
    ...(draft.brandCode ? {manufacturer: draft.brandCode} : {}),
    // A language with nothing in it is not sent: an empty description would overwrite whatever the
    // storefront falls back to with a blank.
    descriptions: draft.copy
      .filter((copy) => copy.name.trim() !== '')
      .map((copy) => ({
        language: copy.language,
        name: copy.name,
        description: copy.description,
        friendlyUrl: copy.friendlyUrl,
        title: copy.title,
        metaDescription: copy.metaDescription,
        highlights: copy.highlights,
        keyWords: copy.keyWords,
      })),
  };
}

/** One vocabulary entry for the axis picker, values in display order. */
function toStoreOption(option: ReadableProductOption): StoreOption {
  return {
    id: option.id,
    code: option.code,
    name: option.name ?? option.descriptions[0]?.name ?? option.code,
    values: [...option.values]
      .sort((left, right) => (left.sortOrder ?? 0) - (right.sortOrder ?? 0))
      .map((value) => ({
        id: value.id,
        code: value.code,
        name: value.name ?? value.descriptions[0]?.name ?? value.code,
      })),
  };
}

/**
 * The axes this product varies by, in display order, read off the variant rows.
 *
 * The variant list is the one read the step makes, and every row carries its resolved
 * `(option, value)` pairs in assignment order — so the axes are the first row's options. Reading
 * them from a second endpoint would be a request for something already in hand.
 */
function assignedOptions(rows: readonly ReadableProductVariantDefinition[]): readonly number[] {
  const first = rows[0];
  if (!first) {
    return [];
  }
  return [...first.optionValues]
    .sort((left, right) => (left.sortOrder ?? 0) - (right.sortOrder ?? 0))
    .map((pair) => pair.optionId);
}

/** One matrix row: the catalog variant with its inventory record merged in. */
function toMatrixRow(
  row: ReadableProductVariantDefinition,
  bySku: ReadonlyMap<string, SkuInventory>,
): VariantMatrixRow {
  const pairs = [...row.optionValues].sort(
    (left, right) => (left.sortOrder ?? 0) - (right.sortOrder ?? 0),
  );
  const stock = bySku.get(row.sku);
  return {
    id: row.id,
    sku: row.sku,
    optionValueIds: pairs.map((pair) => pair.valueId),
    labels: pairs.map((pair) => pair.valueName ?? pair.valueCode),
    isDefault: row.defaultVariant,
    price: stock?.price?.originalPrice ?? stock?.price?.finalPrice ?? null,
    quantity: stock?.quantity ?? 0,
    // A sku with no inventory record yet defaults to sellable, like the simple product's draft.
    available: stock?.available ?? true,
  };
}

function flattenCategories(
  categories: readonly ReadableCategory[],
  depth: number,
): readonly CategoryOption[] {
  return categories.flatMap((category) => [
    {
      id: category.id,
      label: category.description?.name ?? category.descriptions[0]?.name ?? category.code,
      depth,
    },
    ...flattenCategories(category.children, depth + 1),
  ]);
}
