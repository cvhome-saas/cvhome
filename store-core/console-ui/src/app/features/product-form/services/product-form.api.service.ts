import {Injectable, inject} from '@angular/core';
import {Observable, catchError, concat, forkJoin, last, map, of, switchMap, toArray} from 'rxjs';

import {CatalogReference} from '@api/catalog/catalog-reference.service';
import {CategoryService} from '@api/catalog/category.service';
import {ProductImageService} from '@api/catalog/product-image.service';
import {ProductRelationshipService} from '@api/catalog/product-relationship.service';
import {ProductService} from '@api/catalog/product.service';
import {InventoryService} from '@api/inventory/inventory.service';
import type {
  PersistableProductDefinition,
  ProductDescription,
  ReadableCategory,
  ReadableProductDefinition,
  SkuInventory,
} from '@models/catalog';
import type {ProductDraft, ProductImageItem, RelatedProduct} from '@models/products';
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
  private readonly inventory = inject(InventoryService);
  private readonly images = inject(ProductImageService);
  private readonly relationships = inject(ProductRelationshipService);
  private readonly categories = inject(CategoryService);
  private readonly reference = inject(CatalogReference);

  /** The definition the server last sent, for fields the form does not edit but must not clear. */
  private loaded: ReadableProductDefinition | null = null;

  /**
   * A product, or a blank one.
   *
   * The three reference lists are optional in the usual sense — a brand list that fails costs the
   * Organize step its brand select, not the whole form. The product itself is not: a form bound to
   * nothing is not a form.
   */
  load(productId: number | null): Observable<ProductFormSnapshot> {
    return forkJoin({
      /*
       * Price, quantity and purchasability come from the inventory service since the split, keyed
       * by the definition's SKU — so the two reads are chained, not parallel. Optional in the same
       * sense as the reference lists: a form without stock numbers beats no form.
       */
      definition:
        productId === null
          ? of(null)
          : this.products.definition(productId).pipe(
              switchMap((definition) =>
                this.optional(this.inventory.bySkus([definition.sku ?? ''])).pipe(
                  map((inventories) => ({definition, stock: inventories?.[0] ?? null})),
                ),
              ),
            ),
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
      map(({definition: loadedDefinition, related, categories, brands, types, store}) => {
        const definition = loadedDefinition?.definition ?? null;
        const stock = loadedDefinition?.stock ?? null;
        this.loaded = definition;
        const languages = store?.supportedLanguages?.length
          ? [...store.supportedLanguages]
          : [store?.defaultLanguage ?? 'en'];

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
  update(productId: number, draft: ProductDraft): Observable<UpdateOutcome> {
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
      switchMap((categoriesApplied) =>
        this.applyInventory(draft, productId).pipe(
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

  /* ---------------------------------------------------------------------- images ---- */

  /**
   * Upload images and answer the product's whole gallery afterwards.
   *
   * `defaultImage` is sent only when the product has none — see `ProductImageService.upload` for
   * why that is the only honest value: no endpoint re-designates a default, and passing `true` for
   * a product that already has one leaves two.
   *
   * `concat` again: the pod assigns `sortOrder` from the `order` parameter and reads the existing
   * image set to decide the default, so two uploads racing would both see "no default yet".
   */
  uploadImages(
    productId: number,
    files: readonly File[],
    existing: readonly ProductImageItem[],
  ): Observable<readonly ProductImageItem[]> {
    const startOrder = existing.reduce((max, image) => Math.max(max, image.order), -1) + 1;
    const hasDefault = existing.some((image) => image.isDefault);
    const uploads = files.map((file, index) =>
      this.images.upload(productId, file, startOrder + index, !hasDefault && index === 0),
    );
    return this.settling(productId, concat(...uploads));
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
   * Move an image to a new position, renumbering the ones it displaced.
   *
   * `PATCH …?order=` sets one image's `sortOrder` and does not renumber its neighbours, so moving
   * an image without rewriting the rest leaves two images sharing a position and the gallery in
   * whatever order the database returns. The whole list is renumbered, in order.
   */
  reorderImages(
    productId: number,
    ordered: readonly ProductImageItem[],
  ): Observable<readonly ProductImageItem[]> {
    const calls = ordered.map((image, index) => this.images.reorder(productId, image.id, index));
    return this.settling(productId, concat(...calls));
  }

  private gallery(productId: number): Observable<readonly ProductImageItem[]> {
    return this.images.images(productId).pipe(
      map((images) =>
        [...images]
          .sort((left, right) => (left.order ?? 0) - (right.order ?? 0))
          .map((image) => ({
            id: image.id,
            name: image.imageName ?? String(image.id),
            url: image.imageUrl ?? image.externalUrl ?? null,
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
        name: image.imageName ?? String(image.id),
        url: image.imageUrl ?? image.externalUrl ?? null,
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
