import {Component, computed, effect, inject, input, signal, untracked} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {AuditRow, PlatformInvoiceRow, PlatformSubscriptionRow} from '@models/platform-billing';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {BarChart} from '@shared/ui/charts/bar-chart';
import {DonutChart} from '@shared/ui/charts/donut-chart';
import {TrendChart} from '@shared/ui/charts/trend-chart';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {DateRangePicker} from '@shared/ui/date-range-picker/date-range-picker';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {Icon} from '@shared/ui/icon/icon';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {SectionNav} from '@shared/ui/section-nav/section-nav';
import {Select} from '@shared/ui/select/select';
import {TabSwitcher, type TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {Toggle} from '@shared/ui/toggle/toggle';
import {StoreBillingPanel} from './components/store-billing-panel/store-billing-panel';
import {
  BILLING_SECTIONS,
  PAGE_SIZE,
  PlatformBillingFacade,
  type BillingSection,
} from './facades/platform-billing.facade';

/** Which tab keys the route accepts. Anything else settles on `overview`. */
const SECTION_KEYS = new Set<string>(BILLING_SECTIONS.map((section) => section.key));

/** The register's columns. Widths are grid tracks, read straight into the row layout. */
const SUBSCRIPTION_COLUMNS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'store', labelKey: 'platform.billing.subscriptions.column.store', width: 'minmax(11rem, 1.8fr)'},
  {key: 'plan', labelKey: 'platform.billing.subscriptions.column.plan', width: 'minmax(7rem, 1fr)'},
  {key: 'status', labelKey: 'platform.billing.subscriptions.column.status', width: 'minmax(6rem, 0.8fr)'},
  {key: 'amount', labelKey: 'platform.billing.subscriptions.column.amount', width: 'minmax(6rem, 0.8fr)'},
  {key: 'renews', labelKey: 'platform.billing.subscriptions.column.renews', width: 'minmax(7rem, 1fr)'},
];

const INVOICE_COLUMNS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'number', labelKey: 'platform.billing.invoices.column.number', width: 'minmax(9rem, 1.4fr)'},
  {key: 'store', labelKey: 'platform.billing.invoices.column.store', width: 'minmax(9rem, 1.4fr)'},
  {key: 'status', labelKey: 'platform.billing.invoices.column.status', width: 'minmax(6rem, 0.7fr)'},
  {key: 'paid', labelKey: 'platform.billing.invoices.column.paid', width: 'minmax(6rem, 0.8fr)'},
  {key: 'issued', labelKey: 'platform.billing.invoices.column.issued', width: 'minmax(7rem, 0.9fr)'},
  {key: 'links', labelKey: '', width: '4.5rem'},
];

const AUDIT_COLUMNS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'event', labelKey: 'platform.billing.activity.column.event', width: 'minmax(9rem, 1.2fr)'},
  {key: 'store', labelKey: 'platform.billing.activity.column.store', width: 'minmax(9rem, 1.3fr)'},
  {key: 'change', labelKey: 'platform.billing.activity.column.change', width: 'minmax(8rem, 1.1fr)'},
  {key: 'actor', labelKey: 'platform.billing.activity.column.actor', width: 'minmax(8rem, 1.1fr)'},
  {key: 'when', labelKey: 'platform.billing.activity.column.when', width: 'minmax(8rem, 1fr)'},
];

/**
 * Billing, as the platform sees it.
 *
 * **The section this console did not have.** A super admin had no visibility into money at all — not
 * a revenue figure, not a plan count, not an invoice, not a line of the subscription audit trail.
 * `/platform` was two counts of signups and `/platform/plans` was a price list. The data was all
 * there; what was missing was a read path and an audience, and this is both.
 *
 * **The tabs are route segments**, so a tab is linkable and survives a reload — the shape the
 * organization detail and store management already use. Every filter is mirrored into the query
 * string for the same reason, and each is applied *server-side*: they narrow the whole platform
 * rather than the twenty rows on screen.
 *
 * **Every currency is its own figure.** Nothing on this platform holds an exchange rate, so there is
 * no single revenue number anywhere on this page and no attempt to make one — a mixed total would be
 * a wrong number rather than a missing one.
 */
@Component({
  selector: 'app-platform-billing',
  imports: [
    Badge,
    BarChart,
    BusyOverlay,
    DataTable,
    DateRangePicker,
    DonutChart,
    EmptyState,
    Icon,
    KpiGrid,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    SearchBox,
    SectionNav,
    Select,
    StoreBillingPanel,
    TabSwitcher,
    TableRow,
    Toggle,
    TranslocoDirective,
    TrendChart,
  ],
  providers: [PlatformBillingFacade],
  templateUrl: './platform-billing.html',
  styleUrls: ['../../shared/styles/field.css', './platform-billing.css'],
})
export class PlatformBilling {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(PlatformBillingFacade);

  /** The `:section` route param. An unknown value settles on `overview` in the effect below. */
  readonly section = input<string>();

  /** The register's filters, from the query string, so a narrowed list can be linked. */
  readonly q = input<string>();
  readonly status = input<string>();
  readonly plan = input<string>();
  readonly org = input<string>();
  readonly blocked = input<string>();
  readonly page = input<string>();

  /**
   * Whether the section rail is folded to icons.
   *
   * Held here rather than in the rail because the width it animates is a column of *this* grid.
   * Not persisted: nothing on the platform stores operator preferences.
   */
  protected readonly railCollapsed = signal(false);

  protected readonly pageSize = PAGE_SIZE;

  /*
   * Bound once as fields rather than as `facade.x.bind(facade)` in the template: a method reference
   * created in a binding is a new function on every change detection.
   */
  protected readonly statusLabel = (status: string | null) => this.facade.statusLabel(status);
  protected readonly statusTone = (status: string | null) => this.facade.statusTone(status);
  protected readonly invoiceStatusLabel = (status: string | null) => this.facade.invoiceStatusLabel(status);
  protected readonly invoiceStatusTone = (status: string | null) => this.facade.invoiceStatusTone(status);
  protected readonly eventLabel = (event: string | null) => this.facade.eventLabel(event);
  protected readonly eventTone = (event: string | null) => this.facade.eventTone(event);
  protected readonly sourceLabel = (source: string | null) => this.facade.sourceLabel(source);

  protected readonly subscriptionColumns = computed<readonly TableColumn[]>(() =>
    this.columnsOf(SUBSCRIPTION_COLUMNS),
  );
  protected readonly invoiceColumns = computed<readonly TableColumn[]>(() => this.columnsOf(INVOICE_COLUMNS));
  protected readonly auditColumns = computed<readonly TableColumn[]>(() => this.columnsOf(AUDIT_COLUMNS));

  /** The narrow-layout equivalent of the rail. Same keys, same order. */
  protected readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return BILLING_SECTIONS.map((section) => ({
      key: section.key,
      label: this.transloco.translate(section.labelKey),
    }));
  });

  constructor() {
    /*
     * An unknown segment settles on `overview` here rather than being matched in the route: a fixed
     * `:section` list there would make adding a tab a two-file change.
     */
    effect(() => {
      const requested = this.section() ?? 'overview';
      const settled = (SECTION_KEYS.has(requested) ? requested : 'overview') as BillingSection;
      if (settled !== untracked(() => this.facade.section())) {
        this.facade.setSection(settled);
      }
    });

    /*
     * The URL is what a reload and a shared link restore the filters from, and the `on*` handlers
     * below are the other writer — so these effects only carry the cases the page did not cause: a
     * first render, a back button, a pasted link.
     *
     * **Each reads its own signal untracked, and that is load-bearing.** The handler sets the state
     * and *then* navigates, so an effect woken by its own subject would see the new value beside a
     * URL that had not caught up and would immediately put the old one back.
     */
    effect(() => this.restore(this.q() ?? '', this.searchSignal()));
    effect(() => this.restore(this.status() ?? '', this.statusSignal()));

    effect(() => {
      const plan = this.plan() ?? '';
      if (plan !== untracked(() => this.facade.subscriptionPlan())) {
        this.facade.subscriptionPlan.set(plan);
      }
    });

    effect(() => this.restore(this.org() ?? '', this.orgSignal()));

    effect(() => {
      const only = this.blocked() === 'true';
      if (only !== untracked(() => this.facade.blockedOnly())) {
        this.facade.blockedOnly.set(only);
      }
    });

    effect(() => {
      const requested = Number(this.page() ?? 0);
      const index = Number.isFinite(requested) && requested > 0 ? Math.floor(requested) : 0;
      const current = untracked(() => this.pageSignal()());
      if (index !== current) {
        this.pageSignal().set(index);
      }
    });
  }

  /* ------------------------------------------------------------------------ writers ---- */

  protected pickSection(key: string): void {
    // The filters are per-register and named the same in the URL, so switching tabs clears them
    // rather than carrying an invoice status onto the audit trail as an event type.
    void this.router.navigate(['/platform/billing', key], {queryParams: {}});
  }

  /** The state leads and the URL mirrors it — a table that waited for the navigation would lag. */
  protected onSearch(term: string): void {
    this.searchSignal().set(term);
    this.mirror({q: term || null, page: null});
  }

  protected onStatus(status: string): void {
    this.statusSignal().set(status);
    this.mirror({status: status || null, page: null});
  }

  protected onPlan(plan: string): void {
    this.facade.subscriptionPlan.set(plan);
    this.mirror({plan: plan || null, page: null});
  }

  protected onOrg(org: string): void {
    this.orgSignal().set(org);
    this.mirror({org: org || null, page: null});
  }

  protected onBlockedOnly(only: boolean): void {
    this.facade.blockedOnly.set(only);
    this.mirror({blocked: only ? 'true' : null, page: null});
  }

  protected onPage(page: number): void {
    this.pageSignal().set(page);
    this.mirror({page: page ? String(page) : null});
  }

  /**
   * Opens the blocked register from the Overview.
   *
   * Navigates rather than setting the filter directly, so the tab the operator lands on is the one
   * their back button returns from — and so the resulting list is a URL they can send to someone.
   */
  protected openBlocked(): void {
    void this.router.navigate(['/platform/billing', 'subscriptions'], {queryParams: {blocked: 'true'}});
  }

  protected openStore(store: string): void {
    this.facade.openStorePanel(store);
  }

  protected openSubscription(row: PlatformSubscriptionRow): void {
    this.openStore(row.store);
  }

  protected openInvoice(row: PlatformInvoiceRow): void {
    this.openStore(row.store);
  }

  protected openAudit(row: AuditRow): void {
    this.openStore(row.store);
  }

  /* ---------------------------------------------------------------------- internals ---- */

  /**
   * The signal the URL's `q`, `status`, `org` and `page` refer to on the open tab.
   *
   * One set of query-parameter names across four registers rather than four prefixed sets: an
   * operator reading `?status=PAID` on the invoices tab and `?status=PAST_DUE` on the subscriptions
   * tab is reading the same word about the same row, and `pickSection` clears them on the way out so
   * the two can never be confused.
   */
  private searchSignal() {
    switch (this.facade.section()) {
      case 'invoices':
        return this.facade.invoiceStore;
      case 'activity':
        return this.facade.auditStore;
      default:
        return this.facade.subscriptionSearch;
    }
  }

  private statusSignal() {
    switch (this.facade.section()) {
      case 'invoices':
        return this.facade.invoiceStatus;
      case 'activity':
        return this.facade.auditEvent;
      default:
        return this.facade.subscriptionStatus;
    }
  }

  private orgSignal() {
    switch (this.facade.section()) {
      case 'invoices':
        return this.facade.invoiceOrg;
      case 'activity':
        return this.facade.auditOrg;
      default:
        return this.facade.subscriptionOrg;
    }
  }

  private pageSignal() {
    switch (this.facade.section()) {
      case 'invoices':
        return this.facade.invoicePage;
      case 'activity':
        return this.facade.auditPage;
      default:
        return this.facade.subscriptionPage;
    }
  }

  private restore(fromUrl: string, target: {(): string; set(value: string): void}): void {
    if (fromUrl !== untracked(() => target())) {
      target.set(fromUrl);
    }
  }

  private mirror(queryParams: Record<string, string | null>): void {
    void this.router.navigate([], {queryParams, queryParamsHandling: 'merge'});
  }

  private columnsOf(
    keys: readonly {key: string; labelKey: string; width: string}[],
  ): readonly TableColumn[] {
    this.transloco.activeLang();
    return keys.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  }
}
