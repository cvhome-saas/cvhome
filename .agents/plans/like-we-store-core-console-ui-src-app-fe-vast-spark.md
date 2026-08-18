# Orders page for console-ui, from `simple-orders.dc.html`

## Context

`../../store-core/console-ui` is the Angular rewrite of the seller console. It currently has three
pages — marketing, auth, dashboard — built from the mockups in `../../store-core/console-template`.
The console shell (plan banner, navigation rail, toolbar, store switcher) is already
implemented in `src/app/layouts/console-shell/`, and `features/dashboard` is the reference for
how a page is put together: a thin component over a root-provided facade over a mock API
service, styled in plain CSS against the theme tokens.

`simple-orders.dc.html` is the next page. It is `Orders.dc.html` with the "Fulfilment
pipeline", "Shipments in transit" and "Return requests" panels removed — their data is left
dead in the mockup's logic block, and the two blank gaps at lines 153 and 245 are where they
used to be. So the page to build is: **page header → 4 KPI cards → one "All orders" table
panel**. Everything else in that file (banner, sidebar, toolbar) is shell that already exists.

The outcome is a working `/orders` route reachable from the sidebar, visually faithful to the
mockup but expressed in the app's design tokens rather than its hardcoded emerald hexes, with
status tabs and pagination that genuinely filter and page.

## Decisions taken

- **Fully working table.** Tabs filter rows and swap the subtitle and bulk bar; the pager pages
  real data. Row clicks and bulk actions are stubs that raise a toast, since Order Details and
  the fulfilment backend do not exist.
- **Shared components.** The table, tab switcher, pager and notice bar go in `shared/ui/` for
  the Payments / Inventory / Customers pages that follow.
- **Route + nav wired**, with sidebar `active` derived from the router instead of hardcoded.
- **Reuse `app-export-button`** (PDF) rather than writing a CSV exporter; label stays "Export".
- **No hardcoded colours.** The mockup's palette maps exactly onto the existing `Tone`
  vocabulary — Ordered→`green`, Processed→`blue`, Delivered→`cyan`, Refunded→`amber`,
  Canceled→`red` — which is already what `DASHBOARD_ORDER_STATUSES` uses.
- **Skip `TableStateService`.** Its docblock (`src/app/core/table/table-state.service.ts:19`)
  names a `order-list.facade.ts` as its reference implementation, but the service is imperative
  (`setPage`/`setLoading`) and unused, and it would fight `rxResource`, which owns loading state
  declaratively. Follow the live dashboard pattern instead, but **do** reuse the `PageT<T>` /
  `PageRequest` envelope from `core/table/table.types.ts` so the mock API already speaks the
  shape a real backend returns.

## Work

### 1. Icons — `src/app/shared/ui/icon/icon-paths.ts`

Add 8 entries to `ICON_PATHS` (24×24 stroke paths, matching the existing style; `IconName`
widens automatically): `chartLine`, `checkCircle`, `xCircle`, `alertCircle`, `truck`,
`printer`, `filter`, `send`.

### 2. Shared UI components

**`shared/ui/data-table/`** — replace the current stub (a bare `ng-content` box) with a real
primitive that owns the surface, column alignment and header, while the consumer keeps control
of cell content:

```ts
export interface TableColumn {
  readonly key: string;
  readonly label: string;        // '' renders an unlabelled track (checkbox, actions)
  readonly width: string;        // a grid track: '34px', '1.5fr'
  readonly align?: 'start' | 'end';
}
```

`app-data-table` inputs `columns: readonly TableColumn[]` and `label: string`. It sets
`--table-columns` from the column widths on its host, renders the `role="columnheader"` row,
and projects rows. `container-type: inline-size` on the host, per the `kpi-grid` / `bar-chart`
convention. Rows are projected siblings, so they inherit `--table-columns` — that is what keeps
header and body aligned. ARIA roles (`table`/`row`/`columnheader`/`cell`) on the grid divs
rather than a real `<table>`, because each row is an interactive element.

**`shared/ui/data-table/table-row.ts`** — `app-table-row`, applying
`grid-template-columns: var(--table-columns)` and the hover/border rules.

**`shared/ui/tab-switcher/`** — the pill track `DESIGN.md:404` specifies. Inputs
`tabs: readonly TabItem[]` (`{key, label, badge?, badgeTone?}`) and a two-way
`active = model<string>()`. Uses `role="tablist"` with arrow-key roving focus.

**`shared/ui/pagination/`** — inputs `page` (0-based), `totalPages`, `totalElements`,
`pageSize`; output `pageChange`. Renders the "Showing 1–10 of 40" caption plus a windowed page
list with previous/next arrows (`chevronLeft`/`chevronRight` with `[flip]="true"` for RTL).

**`shared/ui/notice-bar/`** — the amber inline notice. Inputs `tone: Tone`, `icon: IconName`,
`message: string`; projects actions. Worth sharing: `dashboard.css`'s `.load-error` block is the
same thing hand-rolled, and can migrate to it later.

**`shared/ui/badge/badge.ts`** — reuse as-is for status chips; the orders page is its first
consumer. Only change: the mockup's chips are `4px`-radius rectangles, so add a
`shape: 'pill' | 'square'` input rather than forcing `rounded-full`.

### 3. Model and fixture

**`src/app/models/orders.ts`**

```ts
export type OrderStatus  = 'Ordered' | 'Processed' | 'Delivered' | 'Refunded' | 'Canceled';
export type PaymentState = 'Paid' | 'Pending' | 'Refunded' | 'Failed';
export type OrderChannel = 'Web' | 'Phone';

export interface OrderRow {
  readonly id: string;              // '#10482'
  readonly channel: OrderChannel;
  readonly customer: string;
  readonly city: string;
  readonly status: OrderStatus;
  readonly payment: PaymentState;
  readonly paymentMeta: string;     // 'Visa •••• 4242'
  readonly items: number;
  readonly total: string;
  readonly placedOn: string;        // 'Aug 4, 2026'
  readonly placedAt: string;        // '09:41'
  readonly unfulfilledFor?: string; // '26h unfulfilled' — presence means late
}

export interface OrdersSnapshot {
  readonly kpis: readonly KpiDatum[];
  readonly page: PageT<OrderRow>;   // from @core/table/table.types
  readonly totalInRange: number;
}
```

Plus exported `STATUS_TONE: Record<OrderStatus, Tone>` and
`PAYMENT_BADGE: Record<PaymentState, {icon: IconName; tone: Tone}>` maps, replacing the
mockup's inline `S` / `PAY` style dictionaries. `initials` and `statusStyle` are **not** stored —
initials derive in the template, tone comes from the map.

**`src/app/mocks/orders.fixture.ts`** — the mockup's 13 orders, extended to ~40 so pagination
is real (4 pages at 10/page) rather than the mockup's decorative `1 2 3`. Keep the mockup's 13
verbatim as the first entries so the default view matches it row for row.

### 4. Feature — `src/app/features/orders/`

Mirrors `features/dashboard/` file for file.

**`services/orders.api.service.ts`** — mock, no `HttpClient`, same shape as `DashboardApi`:
`MIN_LATENCY_MS`/`MAX_LATENCY_MS`/`FAILURE_RATE` constants, `of(...).pipe(delay(latency))`.
`loadOrders(query: OrdersQuery): Observable<OrdersSnapshot>` filters the fixture by tab and
channel, scales counts by the range span deterministically (as `snapshotFor` does), then slices
the requested page into a `PageT<OrderRow>`.

**`facades/orders.facade.ts`** — `@Injectable({providedIn: 'root'})`, following
`DashboardFacade` exactly: `rxResource` keyed on a computed query, `linkedSignal` holding the
last good snapshot so the table does not blank between requests, and `isLoading` / `error` /
`isEmpty` / `retry()` with the same meanings. Adds:

- `dateRange`, `activeTab`, `channel` filter signals
- `pageIndex` as a `linkedSignal` **sourced on the filter tuple with a computation returning 0**,
  so changing tab, channel or range resets to the first page — the one piece of paging logic
  that is easy to get wrong
- `tableSubtitle` from a `SUBS`-equivalent map, `showBulkNotice` (`activeTab() === 'Ordered'`
  and at least one late row), `lateCount`, and `tabs` with the live badge count

**`orders.ts` / `orders.html` / `orders.css`** — structure copied from the dashboard page:
`app-page-header` (with `app-date-range-picker`, `app-export-button`, and a "Create order"
button) → the `load-error` block → `app-busy-overlay` wrapping either `.first-load` or the
content region → `app-kpi-grid` → the orders panel. `:host { display: contents }`; the shell's
`.workspace` gap owns vertical rhythm.

The panel composes `app-panel` for the card chrome, with the tab switcher and channel filter
projected as `[panelAction]`, then `app-notice-bar`, `app-data-table` + `app-table-row` rows,
and `app-pagination` in the footer. The channel filter is page-local, built on the global
`.popover` class the toolbar and store switcher already share.

**`orders.spec.ts`** — clone `dashboard.spec.ts`'s `FakeDashboardApi` harness (with its
`deferred` / `pending: Subject` / `failure` knobs and `fakeAsync` + `tick()`), then assert:
rows render, switching tabs refetches with the new tab and resets `page` to 0, the pager
requests the next page, the bulk notice appears only on Ordered, chrome is absent, and retry
recovers from an error.

### 5. Responsive and RTL

The mockup is `min-width: 1280px` desktop-only; this needs to survive the shell's existing
breakpoints (rail collapses at 900px). Using `@container` queries on the table, per the
`kpi-grid` / `bar-chart` convention, so it responds to its own column rather than the viewport:

- **wide** — the full 9-track grid as mocked
- **medium** — the grid keeps its minimum width and the table scrolls horizontally inside the
  panel, header row sticky; the panel header wraps its tabs onto a second line
- **narrow (< ~600px)** — rows restack into cards: each cell becomes a labelled row via
  `data-label` attributes and `::before { content: attr(data-label) }`

KPI columns already step 4→2→1 on their own. Throughout, logical properties
(`inline-size`, `padding-inline`, `text-align: end`) and `[flip]="true"` on directional
chevrons, so RTL works as it does elsewhere.

### 6. Wiring

- `src/app/app.routes.ts` — an `orders` block copying the `dashboard` one:
  `ConsoleShell` parent, lazy `path: ''` child, `title` via
  `$localize\`:@@route.orders.title:Orders | cvhome\``, and `data: {breadcrumb: 'Orders'}` for
  the toolbar. Page copy stays plain text, matching `dashboard.html`.
- `src/app/app.routes.server.ts` — `{path: 'orders', renderMode: RenderMode.Client}`.
- `src/app/mocks/console.fixture.ts` — give the Orders nav item `route: '/orders'`.
- `console-sidebar.ts` — drop the hardcoded `active: true` on Home and use `routerLinkActive`
  so the rail highlights whichever page is open. (Small, but without it Home stays lit on
  `/orders`.)
- `src/locale/messages.xlf` — add the new route title; leave the `.ar`/`.fr`/`.de` files for the
  next `npm run extract:i18n`.

## Verification

1. `npm run lint` and `npm test` — the new `orders.spec.ts` must pass alongside the existing suites.
2. `npm start`, then open `/orders`:
   - the sidebar's Orders item is highlighted and navigates; the toolbar breadcrumb reads
     `Dashboard › Orders`
   - clicking each status tab changes the rows, the panel subtitle, and the pager total, and
     drops back to page 1; the amber notice appears only on Ordered
   - the pager moves through all 4 pages and the "Showing X–Y of Z" caption tracks it
   - changing the date range re-fetches: the veil appears and the previous rows stay visible
     underneath rather than the layout collapsing
   - Export produces a PDF of the content region
3. Compare against `../../store-core/console-template/simple-orders.dc.html` side by side at 1440px,
   900px and 420px — the same check the earlier console-ui plan used.
4. Switch themes (forest / midnight / daylight) from the toolbar and confirm no hardcoded
   colour survives; toggle RTL via the language menu's Arabic option and confirm the table,
   chevrons and pager mirror.
5. Set `FAILURE_RATE = 1` in `orders.api.service.ts` temporarily to exercise the error block and
   `Try again`.
