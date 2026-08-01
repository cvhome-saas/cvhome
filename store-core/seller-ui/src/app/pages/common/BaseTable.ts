export interface PageRequest {
  count: number,
  page: number
}

export interface StorePageRequest extends PageRequest {
  store?: string,
}

export class PageT<T> {
  size = 0;
  totalElements = 0;
  totalPages = 0;
  pageNumber = 0;
  content: T[] = [];
}
