/**
 * The paging envelope, on the wire and in the console.
 *
 * A page is a shape the *server* sends, so it belongs at the bottom tier beside the DTOs that are
 * paged: `models/orders.ts` and `models/products.ts` were both reaching up into the console's
 * `@core/table` for `PageT`, which inverted the declared direction for a type that was never about
 * the table component in the first place.
 *
 * A `core/table/table.types.ts` used to re-export all of this so those callers could keep writing
 * `@core/table`. It was deleted when this moved into the kit: every caller now writes
 * `@cvhome-saas/ui-kit` whichever file the type sits in, so the indirection bought nothing and its
 * docstring had started describing an arrangement that no longer existed.
 */

export interface PageRequest {
  [key: string]: string | number | boolean | undefined;
  count: number;
  page: number;
}

export interface StorePageRequest extends PageRequest {
  store?: string;
}

export interface PageT<T> {
  size: number;
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  content: T[];
  recordsFiltered?: number;
}

/** Mirrors Spring Data's Page<T> — used by tenancy endpoints that
 *  return the framework's own paging envelope instead of this app's PageT. */
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const EMPTY_PAGE: PageT<never> = {
  size: 0,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};
