import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of, switchMap} from 'rxjs';

import {CatalogReference} from '@api/catalog/catalog-reference.service';
import {CategoryService} from '@api/catalog/category.service';
import {ManufacturerService} from '@api/catalog/manufacturer.service';
import {ProductGroupService} from '@api/catalog/product-group.service';
import {ProductOptionService} from '@api/catalog/product-option.service';
import {ProductService} from '@api/catalog/product.service';
import {ProductTypeService} from '@api/catalog/product-type.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import type {PageT} from '@cvhome-saas/ui-kit';
import type {
  NamedDescription,
  PersistableCategory,
  PersistableProductGroup,
  PersistableProductOption,
  PersistableProductType,
  ReadableCategory,
  ReadableManufacturer,
  ReadableProductGroup,
  ReadableProductOption,
  ReadableProductOptionValue,
  ReadableProductType,
} from '@models/catalog';
import type {
  BrandCard,
  CatalogueSnapshot,
  CatalogueTab,
  CategoryNode,
  GroupRow,
  LocalisedCopy,
  OptionCard,
  OptionName,
  OptionValueCard,
  TypeCard,
} from '@models/taxonomy';

/**
 * How much of each list is asked for in one go.
 *
 * The catalogue is not a paged screen: a tree that shows half a hierarchy is not a tree, and a
 * brand grid that pages is a grid an operator cannot scan. `count` is set high enough that a real
 * store's taxonomy arrives whole. A store past this has outgrown a single-screen editor, and the
 * fix for that is a different screen, not a smaller page.
 */
const CATALOGUE_PAGE = {page: 0, count: 500} as const;

/**
 * Everything `/catalogue` shows, and every write it makes.
 *
 * **The category hierarchy is the page.** It is the one unwrapped leg of the load: if it fails,
 * there is nothing to render and the error belongs on screen. The other three are optional in the
 * `store-settings.api.service.ts` sense — a brand list that 500s should cost the operator the
 * Brands tab, not the tree they were working in — and each records itself in `unavailable` so the
 * tab can say what happened rather than looking empty.
 *
 * **Writes reload rather than echo.** Half these endpoints answer `void` and the other half echo
 * the request body back, which is the operator's own input and not the server's answer — the pod
 * recomputes `lineage`, `depth` and `productCount` on a move, and slugifies a code on a create.
 * Showing the request back would show none of that.
 */
@Injectable({providedIn: 'root'})
export class CatalogueApi {
  private readonly categories = inject(CategoryService);
  private readonly brands = inject(ManufacturerService);
  private readonly types = inject(ProductTypeService);
  private readonly options = inject(ProductOptionService);
  private readonly groups = inject(ProductGroupService);
  private readonly products = inject(ProductService);
  private readonly stores = inject(MerchantStoreService);
  private readonly reference = inject(CatalogReference);

  /** The last hierarchy the server sent, flat and keyed by id. The source for a write's untouched fields. */
  private loadedCategories = new Map<number, ReadableCategory>();
  private loadedBrands = new Map<number, ReadableManufacturer>();
  private loadedTypes = new Map<number, ReadableProductType>();
  private loadedOptions = new Map<number, ReadableProductOption>();
  private loadedGroups = new Map<string, ReadableProductGroup>();

  /**
   * Every group, each with the products it actually holds.
   *
   * **The list endpoint does not carry them.** `GET /private/products/groups` answers
   * `products: []` for every group in the page, no matter what is in them; only
   * `GET /private/products/groups/{code}` populates the array. Building the member lists from the
   * list response therefore showed every group as empty — which is what it did until this was
   * found, and why seller-ui's group screen looked like the only one that worked: it fetches by
   * code, one group at a time.
   *
   * The extra requests are bounded by the number of groups, which is a handful — this is a store's
   * merchandising sets, not its catalogue. `forkJoin` over an empty list completes immediately, so
   * a store with no groups pays nothing.
   */
  private groupsWithProducts(): Observable<PageT<ReadableProductGroup>> {
    return this.groups.list(CATALOGUE_PAGE).pipe(
      switchMap((page) => {
        const codes = page.content.map((group) => group.code).filter((code): code is string => !!code);
        if (codes.length === 0) {
          return of(page);
        }
        return forkJoin(
          // A group that fails to load individually keeps its hollow row rather than taking the tab
          // down: the name is right, and the member list says what it can.
          codes.map((code, index) =>
            this.groups.get(code).pipe(catchError(() => of(page.content[index]))),
          ),
        ).pipe(map((content) => ({...page, content})));
      }),
    );
  }

  /**
   * Re-read after a write.
   *
   * Identical to `load`, except that it first drops the shared reference cache the product form
   * reads from. Without this a brand renamed here would keep its old name in the product form's
   * select for the rest of the session — the cache is what makes navigating between the two cheap,
   * and this is the price of it.
   */
  private reload(): Observable<CatalogueSnapshot> {
    this.reference.invalidate();
    return this.load();
  }

  load(): Observable<CatalogueSnapshot> {
    return forkJoin({
      hierarchy: this.categories.hierarchy(CATALOGUE_PAGE),
      /*
       * Three independent lists on the same pod. They fail independently in practice — a store with
       * no brands answers an empty page, but a store whose product-group table has never been
       * migrated answers a 500 — so each is caught on its own and named in `unavailable`.
       */
      brands: this.optional(this.brands.list(CATALOGUE_PAGE)),
      types: this.optional(this.types.list(CATALOGUE_PAGE)),
      options: this.optional(this.options.list(CATALOGUE_PAGE)),
      groups: this.optional(this.groupsWithProducts()),
      /*
       * Which languages the store trades in — the merchant pod, not the catalog one, and not the
       * console's own en/ar. The locale chips are about the shopper's language, not the operator's.
       * Losing it costs the chips, not the page.
       */
      languages: this.stores.supportedLanguages().pipe(catchError(() => of<string[]>([]))),
    }).pipe(
      map(({hierarchy, brands, types, options, groups, languages}) => {
        this.index(hierarchy.content);
        this.loadedBrands = new Map((brands?.content ?? []).map((brand) => [brand.id, brand]));
        this.loadedTypes = new Map((types?.content ?? []).map((type) => [type.id, type]));
        this.loadedOptions = new Map((options?.content ?? []).map((option) => [option.id, option]));
        this.loadedGroups = new Map(
          (groups?.content ?? []).filter((group) => group.code).map((group) => [group.code as string, group]),
        );

        const unavailable: CatalogueTab[] = [];
        if (brands === null) {
          unavailable.push('brands');
        }
        if (types === null) {
          unavailable.push('types');
        }
        if (options === null) {
          unavailable.push('options');
        }
        if (groups === null) {
          unavailable.push('groups');
        }

        return {
          categories: hierarchy.content.map((category) => toNode(category, null)),
          brands: (brands?.content ?? []).map(toBrand),
          types: (types?.content ?? []).map(toType),
          options: (options?.content ?? []).map(toOption),
          groups: (groups?.content ?? []).map(toGroup),
          languages,
          unavailable,
        };
      }),
    );
  }

  /* -------------------------------------------------------------------- categories ---- */

  /**
   * Create a top-level category, or a child of one.
   *
   * `code` is required and unique per store, and nothing generates one — so it is derived from the
   * name the operator typed, the same way seller-ui derived it, and the uniqueness check runs
   * before the create rather than after the 409.
   */
  createCategory(
    copy: readonly LocalisedCopy[],
    code: string,
    parentId: number | null,
  ): Observable<CatalogueSnapshot> {
    const parent = parentId === null ? undefined : this.loadedCategories.get(parentId);
    const body: PersistableCategory = {
      code,
      visible: false,
      descriptions: copy.map(toDescription),
      ...(parent ? {parent: {id: parent.id, code: parent.code}} : {}),
    };
    return this.categories.create(body).pipe(switchMap(() => this.reload()));
  }

  /**
   * Save a category's copy, slug, sort order and visibility.
   *
   * The whole `PersistableCategory` goes, not a patch: `CategoryFacadeImpl.saveCategory` maps every
   * field onto the entity, so a field left out is a field cleared. `parent` is carried from the
   * loaded record — the tree moves categories with `PUT …/move/{parent}`, and re-sending the parent
   * here keeps a save from silently promoting a child to the root.
   */
  updateCategory(
    id: number,
    copy: readonly LocalisedCopy[],
    fields: {visible: boolean; sortOrder: number},
  ): Observable<CatalogueSnapshot> {
    const loaded = this.loadedCategories.get(id);
    const body: PersistableCategory = {
      id,
      code: loaded?.code ?? '',
      visible: fields.visible,
      sortOrder: fields.sortOrder,
      featured: loaded?.featured,
      descriptions: copy.map(toDescription),
      ...(loaded?.parent ? {parent: loaded.parent} : {}),
    };
    return this.categories.update(id, body).pipe(switchMap(() => this.reload()));
  }

  /**
   * The eye toggle.
   *
   * `PATCH …/visible` reads only `visible` off the body but is bound to a `@Valid
   * PersistableCategory`, so `code` and `descriptions` have to be there or the request is rejected
   * before the handler sees it. Both come from the loaded record.
   */
  setCategoryVisible(id: number, visible: boolean): Observable<CatalogueSnapshot> {
    const loaded = this.loadedCategories.get(id);
    const body: PersistableCategory = {
      id,
      code: loaded?.code ?? '',
      visible,
      descriptions: (loaded?.descriptions ?? []).map((description) => ({...description})),
    };
    return this.categories.setVisible(id, body).pipe(switchMap(() => this.reload()));
  }

  /** Nest one category under another, or promote it to the top level with `moveToRoot`. */
  moveCategory(childId: number, parentId: number | null): Observable<CatalogueSnapshot> {
    const call =
      parentId === null ? this.categories.moveToRoot(childId) : this.categories.move(childId, parentId);
    return call.pipe(switchMap(() => this.reload()));
  }

  deleteCategory(id: number): Observable<CatalogueSnapshot> {
    return this.categories.delete(id).pipe(switchMap(() => this.reload()));
  }

  categoryCodeTaken(code: string): Observable<boolean> {
    return this.categories.codeTaken(code).pipe(map((answer) => answer.exists === true));
  }

  /* ------------------------------------------------------------------------ brands ---- */

  /**
   * Create a brand.
   *
   * `order` is not sent. It is on `PersistableManufacturer` and
   * `PersistableManufacturerPopulator.populate` never reads it — the populator sets the code and,
   * per description, `name`, `description` and `languageCode`, and nothing else. Sending a number
   * the server drops is the kind of thing that reads as working until someone checks.
   */
  createBrand(copy: readonly LocalisedCopy[], code: string): Observable<CatalogueSnapshot> {
    return this.brands
      .create({code, descriptions: copy.map(toDescription)})
      .pipe(switchMap(() => this.reload()));
  }

  updateBrand(id: number, copy: readonly LocalisedCopy[]): Observable<CatalogueSnapshot> {
    const loaded = this.loadedBrands.get(id);
    return this.brands
      .update(id, {id, code: loaded?.code ?? '', descriptions: copy.map(toDescription)})
      .pipe(switchMap(() => this.reload()));
  }

  deleteBrand(id: number): Observable<CatalogueSnapshot> {
    return this.brands.delete(id).pipe(switchMap(() => this.reload()));
  }

  brandCodeTaken(code: string): Observable<boolean> {
    return this.brands.codeTaken(code).pipe(map((answer) => answer.exists === true));
  }

  /* ----------------------------------------------------------------- product types ---- */

  createType(
    copy: readonly LocalisedCopy[],
    code: string,
    fields: {visible: boolean; allowAddToCart: boolean},
  ): Observable<CatalogueSnapshot> {
    const body: PersistableProductType = {code, ...fields, descriptions: copy.map(toDescription)};
    return this.types.create(body).pipe(switchMap(() => this.reload()));
  }

  updateType(
    id: number,
    copy: readonly LocalisedCopy[],
    fields: {visible: boolean; allowAddToCart: boolean},
  ): Observable<CatalogueSnapshot> {
    const loaded = this.loadedTypes.get(id);
    const body: PersistableProductType = {
      id,
      code: loaded?.code ?? '',
      ...fields,
      descriptions: copy.map(toDescription),
    };
    return this.types.update(id, body).pipe(switchMap(() => this.reload()));
  }

  deleteType(id: number): Observable<CatalogueSnapshot> {
    return this.types.delete(id).pipe(switchMap(() => this.reload()));
  }

  typeCodeTaken(code: string): Observable<boolean> {
    return this.types.codeTaken(code).pipe(map((answer) => answer.exists === true));
  }

  /* ---------------------------------------------------------------- product options ---- */

  /**
   * Create a store option with its values — one whole-document write.
   *
   * `sortOrder` on new values is their position in the editor, because the storefront's chips and
   * the variant matrix both render values in this order and an unordered vocabulary shuffles on
   * every read.
   */
  createOption(option: PersistableProductOption): Observable<CatalogueSnapshot> {
    return this.options.create(option).pipe(switchMap(() => this.reload()));
  }

  /**
   * Save an option. The values travel with it; a value carrying its id keeps its row — and its
   * store-wide id, which every variant that sells it references.
   */
  updateOption(id: number, option: PersistableProductOption): Observable<CatalogueSnapshot> {
    return this.options.update(id, {...option, id}).pipe(switchMap(() => this.reload()));
  }

  /**
   * Delete an option. The pod refuses with 409 (`CATALOG.PRODUCT_OPTION.IN_USE`) while any product
   * assigns it or a variant uses one of its values — the facade shows that refusal by name rather
   * than as a generic conflict.
   */
  deleteOption(id: number): Observable<CatalogueSnapshot> {
    return this.options.delete(id).pipe(switchMap(() => this.reload()));
  }

  optionCodeTaken(code: string): Observable<boolean> {
    return this.options.codeTaken(code).pipe(map((answer) => answer.exists === true));
  }

  /** What the selected option's untouched fields hold, for a write that must not clear them. */
  loadedOption(id: number): ReadableProductOption | undefined {
    return this.loadedOptions.get(id);
  }

  /* ---------------------------------------------------------------- product groups ---- */

  /**
   * Create or edit a group.
   *
   * One endpoint for both, because `POST /private/products/groups` is an upsert keyed on `code` and
   * there is no `PUT`. `productIds` is deliberately not sent: membership is managed by the two
   * dedicated endpoints below, and a save that carried the member list would make every copy edit a
   * chance to silently drop a member.
   */
  saveGroup(code: string, copy: readonly LocalisedCopy[], active: boolean): Observable<CatalogueSnapshot> {
    const body: PersistableProductGroup = {code, active, descriptions: copy.map(toDescription)};
    return this.groups.save(body).pipe(switchMap(() => this.reload()));
  }

  /**
   * The active toggle.
   *
   * The upsert replaces the whole record, so flipping one boolean means re-sending the descriptions
   * the group already has — otherwise the toggle would clear the group's names. seller-core spent a
   * round trip re-reading them; the page already holds them, so this does not.
   */
  setGroupActive(code: string, active: boolean): Observable<CatalogueSnapshot> {
    const loaded = this.loadedGroups.get(code);
    const body: PersistableProductGroup = {
      code,
      active,
      descriptions: (loaded?.descriptions ?? []).map((description) => ({...description})),
    };
    return this.groups.save(body).pipe(switchMap(() => this.reload()));
  }

  addGroupMember(code: string, productId: number): Observable<CatalogueSnapshot> {
    return this.groups.addProduct(code, productId).pipe(switchMap(() => this.reload()));
  }

  removeGroupMember(code: string, productId: number): Observable<CatalogueSnapshot> {
    return this.groups.removeProduct(code, productId).pipe(switchMap(() => this.reload()));
  }

  deleteGroup(code: string): Observable<CatalogueSnapshot> {
    return this.groups.delete(code).pipe(switchMap(() => this.reload()));
  }

  groupCodeTaken(code: string): Observable<boolean> {
    return this.groups.codeTaken(code).pipe(map((answer) => answer.exists === true));
  }

  /**
   * Products matching a SKU fragment, for the member picker.
   *
   * By SKU, not by name: the catalogue has no working name filter — see `ProductService.search`.
   * The results still *show* names, so the operator can confirm what they are adding.
   */
  searchProducts(term: string): Observable<readonly {id: number; name: string; sku: string}[]> {
    return this.products
      .search({page: 0, count: 10, sku: term})
      .pipe(
        map((page) =>
          page.content.map((product) => ({
            id: product.id,
            name: product.description?.name ?? product.sku ?? String(product.id),
            sku: product.sku ?? '',
          })),
        ),
        catchError(() => of([])),
      );
  }

  /* ----------------------------------------------------------------------- helpers ---- */

  /** A tab's list, or `null` when its endpoint failed — the tab says so rather than looking empty. */
  private optional<T>(source: Observable<T>): Observable<T | null> {
    return source.pipe(catchError(() => of(null)));
  }

  /** Flattens the hierarchy into a lookup, so a write can reach a node's untouched fields. */
  private index(categories: readonly ReadableCategory[]): void {
    this.loadedCategories = new Map();
    const walk = (nodes: readonly ReadableCategory[]): void => {
      for (const node of nodes) {
        this.loadedCategories.set(node.id, node);
        walk(node.children);
      }
    };
    walk(categories);
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * One wire description as the editors' single copy shape.
 *
 * The seven fields are the same seven under every one of the catalogue's DTOs, which is what lets
 * one editor serve four tabs. `??` rather than `||` on `id`: a description whose id is `0` is still
 * that description.
 */
function toCopy(description: NamedDescription): LocalisedCopy {
  return {
    language: description.language,
    name: description.name ?? '',
    description: description.description ?? '',
    friendlyUrl: description.friendlyUrl ?? '',
    title: description.title ?? '',
    metaDescription: description.metaDescription ?? '',
    highlights: description.highlights ?? '',
    keyWords: description.keyWords ?? '',
  };
}

/** The reverse. Empty strings are sent as-is: they are how a field is cleared. */
function toDescription(copy: LocalisedCopy): NamedDescription {
  return {
    language: copy.language,
    name: copy.name,
    description: copy.description,
    friendlyUrl: copy.friendlyUrl,
    title: copy.title,
    metaDescription: copy.metaDescription,
    highlights: copy.highlights,
    keyWords: copy.keyWords,
  };
}

/**
 * One category and its subtree.
 *
 * `totalCount` rolls the descendants' counts up, because `productCount` is the category's own and a
 * parent with all its products in children would otherwise read as empty.
 */
function toNode(category: ReadableCategory, parentId: number | null): CategoryNode {
  const children = category.children.map((child) => toNode(child, category.id));
  const own = category.productCount ?? 0;
  return {
    id: category.id,
    code: category.code,
    name: category.description?.name ?? category.descriptions[0]?.name ?? category.code,
    visible: category.visible ?? false,
    sortOrder: category.sortOrder ?? 0,
    depth: category.depth ?? 0,
    parentId,
    productCount: own,
    totalCount: children.reduce((sum, child) => sum + child.totalCount, own),
    copy: category.descriptions.map(toCopy),
    children,
  };
}

/**
 * One brand.
 *
 * `descriptions` is narrowed rather than indexed: `ReadableManufacturer` declares the field with no
 * initialiser, so it arrives as `null` on the wire — verified against the running stack, where
 * indexing it took the whole page down on first load.
 */
function toBrand(brand: ReadableManufacturer): BrandCard {
  const descriptions = brand.descriptions ?? [];
  const name = brand.description?.name ?? descriptions[0]?.name ?? brand.code;
  return {
    id: brand.id,
    code: brand.code,
    name,
    description: brand.description?.description ?? descriptions[0]?.description ?? '',
    copy: descriptions.map(toCopy),
    initials: initialsOf(name),
  };
}

/** One product type. `descriptions` is narrowed for the same reason as a brand's. */
function toType(type: ReadableProductType): TypeCard {
  const descriptions = type.descriptions ?? [];
  return {
    id: type.id,
    code: type.code,
    name: type.description?.name ?? descriptions[0]?.name ?? type.code,
    description: type.description?.description ?? descriptions[0]?.description ?? '',
    visible: type.visible ?? false,
    allowAddToCart: type.allowAddToCart ?? true,
    copy: descriptions.map(toCopy),
  };
}

/**
 * One store option with its values.
 *
 * Values are ordered by `sortOrder` here so the editor, the variant matrix and the storefront's
 * chips all agree on an order the server does not guarantee in the list response.
 */
function toOption(option: ReadableProductOption): OptionCard {
  return {
    id: option.id,
    code: option.code,
    name: option.name ?? option.descriptions[0]?.name ?? option.code,
    sortOrder: option.sortOrder ?? 0,
    copy: option.descriptions.map(toOptionName),
    values: [...option.values]
      .sort((left, right) => (left.sortOrder ?? 0) - (right.sortOrder ?? 0))
      .map(toOptionValue),
  };
}

function toOptionValue(value: ReadableProductOptionValue): OptionValueCard {
  return {
    id: value.id,
    code: value.code,
    name: value.name ?? value.descriptions[0]?.name ?? value.code,
    sortOrder: value.sortOrder ?? 0,
    copy: value.descriptions.map(toOptionName),
  };
}

/** Only `name` renders anywhere on an option — see `OptionName` in `@models/taxonomy`. */
function toOptionName(description: NamedDescription): OptionName {
  return {language: description.language, name: description.name ?? ''};
}

function toGroup(group: ReadableProductGroup): GroupRow {
  const code = group.code ?? '';
  return {
    code,
    name: group.description?.name ?? group.descriptions[0]?.name ?? code,
    active: group.active ?? false,
    copy: group.descriptions.map(toCopy),
    members: group.products.map((product) => ({
      id: product.id,
      name: product.description?.name ?? product.sku ?? String(product.id),
      sku: product.sku ?? '',
    })),
  };
}

/** First letters of the first two words. The brand card's mark, since there is no logo. */
function initialsOf(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word.charAt(0))
    .join('')
    .toUpperCase();
}
