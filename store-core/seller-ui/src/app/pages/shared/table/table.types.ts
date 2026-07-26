export interface PageRequest {
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
}

export const EMPTY_PAGE: PageT<never> = {
  size: 0,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};
