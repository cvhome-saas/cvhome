/**
 * The paging types, re-exported from the tier they belong to.
 *
 * They describe what the server sends, so they live in `@models/page`; this file stays because the
 * api tier, the table and every `PageRequest` caller already import from here, and because
 * `@core/table` is the honest place for a *table's* view of a page.
 */
export type {PageRequest, StorePageRequest, PageT, SpringPage} from '@models/page';
export {EMPTY_PAGE} from '@models/page';
