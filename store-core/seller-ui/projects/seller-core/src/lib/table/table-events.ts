/** Structural shape of the application's table pagination output.
 *  Declared locally on purpose — not imported from the library.
 *  TODO(types): ngx-datatable v22 ships real event types; swap when convenient. */
export interface DatatablePageEvent {
  offset: number;
  pageSize: number;
  limit: number;
  count: number;
}

/** TODO(types): ngx-datatable v22 ships real event types; swap when convenient. */
export interface DatatableActivateEvent<T> {
  type: string;
  event: Event;
  row: T;
  rowElement?: unknown;
  column?: unknown;
}

/** TODO(types): ngx-datatable v22 ships real event types; swap when convenient. */
export interface DatatableSelectEvent<T> {
  selected: T[];
}
