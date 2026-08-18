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
