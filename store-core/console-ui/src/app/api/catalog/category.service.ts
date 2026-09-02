import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {PageRequest, PageT} from '@cvhome-saas/ui-kit';
import type {
  EntityExists,
  PersistableCategory,
  ReadableCategory,
} from '@models/catalog';

const CATALOG_API_BASE = '/spg/catalog/api/v1';

/**
 * The parent id that means "no parent".
 *
 * `CategoryFacadeImpl.move` reads `-1` as a request to detach — `addChild(null, category)` — and no
 * other value expresses it.
 */
export const ROOT_PARENT = -1;

/**
 * Categories, and the tree they form. `?store=` is stamped by `CrudService` from the request
 * context — no caller passes it.
 *
 * **`hierarchy()` is the page, and `list()` is not used by it.** `GET /private/category` returns a
 * flat page of every category; `GET /private/category-hierarchy` returns only the roots, each with
 * its `children` populated. The console draws a tree, so it asks for the tree — flattening a page
 * back into one on the client would mean reconstructing parentage the server already knows.
 */
@Injectable({providedIn: 'root'})
export class CategoryService {
  private readonly crudService = inject(CrudService);

  /**
   * Every root category, with its descendants nested underneath.
   *
   * Answers `ReadableCategoryList extends ReadableList<ReadableCategory>` — the platform's paging
   * envelope — even though the hierarchy is not meaningfully pageable. `count` is passed high
   * enough that a store's whole tree arrives in one answer; a store with more root categories than
   * that has a different problem than paging would solve.
   */
  hierarchy(params?: Partial<PageRequest>): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/category-hierarchy`, params);
  }

  /** The flat list. Used for the product form's category picker, where nesting is decoration. */
  list(params?: Partial<PageRequest>): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/category`, params);
  }

  get(id: number): Observable<ReadableCategory> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/category/${id}`);
  }

  /** Which categories a product is in. The read half of the Organize step's diff. */
  forProduct(productId: number): Observable<PageT<ReadableCategory>> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/category/product/${productId}`);
  }

  /** Echoes the body back, with the new id on it. */
  create(category: PersistableCategory): Observable<PersistableCategory> {
    return this.crudService.post(`${CATALOG_API_BASE}/private/category`, category);
  }

  update(id: number, category: PersistableCategory): Observable<PersistableCategory> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/category/${id}`, category);
  }

  /**
   * The eye toggle.
   *
   * A `PATCH` that reads only `visible` off the body, but is bound to a whole `@Valid
   * PersistableCategory` — so `code` and `descriptions` have to be present or the request is a 400
   * before it reaches the handler. The caller sends the category it already loaded.
   */
  setVisible(id: number, category: PersistableCategory): Observable<void> {
    return this.crudService.patch(`${CATALOG_API_BASE}/private/category/${id}/visible`, category);
  }

  /**
   * Re-parent one category under another.
   *
   * The body is ignored; the two path variables are the whole request. The pod recomputes `lineage`
   * and `depth` for the moved subtree, which is why the console reloads the tree afterwards rather
   * than moving the node locally.
   *
   * **`-1` is how a category is promoted to the top level.** `CategoryFacadeImpl.move` special-cases
   * it — `if (parent == -1) categoryService.addChild(null, c)` — and there is no other way to detach
   * a child, because `PUT …/category/{id}` cannot clear a parent it is not given. seller-ui never
   * found this: its tree only nests. Named here rather than left as a magic number at the call site.
   */
  move(childId: number, parentId: number): Observable<void> {
    return this.crudService.put(`${CATALOG_API_BASE}/private/category/${childId}/move/${parentId}`, {});
  }

  /** Detach a category from its parent, making it top-level. See `move`. */
  moveToRoot(childId: number): Observable<void> {
    return this.move(childId, ROOT_PARENT);
  }

  delete(id: number): Observable<void> {
    return this.crudService.delete(`${CATALOG_API_BASE}/private/category/${id}`);
  }

  codeTaken(code: string): Observable<EntityExists> {
    return this.crudService.get(`${CATALOG_API_BASE}/private/category/unique`, {code});
  }
}
