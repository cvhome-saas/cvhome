/** Structural shape of the application's table pagination output.
 *  Declared locally on purpose — not imported from the library.
 *  Kept local because console-ui does not ship a datatable dependency yet. */
export interface DatatablePageEvent {
  offset: number;
  pageSize: number;
  limit: number;
  count: number;
}

/** Row activation emitted by a future table adapter. */
export interface DatatableActivateEvent<T> {
  type: string;
  event: Event;
  row: T;
  rowElement?: unknown;
  column?: unknown;
}

/** Selection emitted by a future table adapter. */
export interface DatatableSelectEvent<T> {
  selected: T[];
}
