/**
 * The paging envelope, on the wire and in the console.
 *
 * A page is a shape the *server* sends, so it belongs at the bottom tier beside the DTOs that are
 * paged: `models/orders.ts` and `models/products.ts` were both reaching up into `@core/table` for
 * `PageT`, which inverted the declared direction for a type that was never about the table
 * component in the first place.
 *
 * `@core/table/table.types` re-exports all of this, so the api tier and the table keep their
 * existing import and nothing has to know it moved.
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
