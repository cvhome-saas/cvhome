import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {STATUS_TONE} from '@models/orders';
import type {CustomerOrderRow, CustomerRow} from '@models/customers';
import type {Tone} from '@cvhome-saas/ui-kit';
import {Money} from '@shared/i18n/money';
import {StatusLabel} from '@shared/i18n/status-label';
import {snapshot} from '@cvhome-saas/ui-kit';
import {CustomersApi} from '../services/customers.api.service';

export const PAGE_SIZE = 20;

/**
 * The customers page.
 *
 * One load answers the table; a second runs only when someone opens a customer. That shape is
 * forced by the backend rather than chosen: there is no per-customer aggregate to put in a column,
 * and no get-by-id to fetch a detail with, so the table is the list endpoint and the dialog is a
 * row that is already in hand plus that customer's orders.
 *
 * **The list is scoped to the open store**, like every other console page — `CustomerApi.list`
 * filters on `storeMerchantId`, so a shopper who has bought from two of the merchant's stores is a
 * separate record in each. The page header already says which store is open, so the page does not
 * repeat it.
 */
@Injectable()
export class CustomersFacade {
  private readonly api = inject(CustomersApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly statusLabels = inject(StatusLabel);
  private readonly money = inject(Money);

  /** Which customer the dialog is showing, by id. Mirrored into the URL by the page. */
  readonly selectedId = signal<number | null>(null);

  /**
   * The search term, as sent to the server.
   *
   * One box against one parameter: `CustomerApi.list`'s `name` already spans the billing first
   * name, the billing last name and the email address, so there is nothing to route by shape the
   * way the orders page has to.
   */
  readonly search = signal('');

  /**
   * The page being read.
   *
   * A `linkedSignal` over the store **and the term**, so both switching stores and typing a new
   * search drop the reader back to the first page rather than asking for page 4 of a smaller result.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.shell.currentStoreId(), this.search()] as const,
    computation: () => 0,
  });

  /**
   * What the table is a reading of.
   *
   * `undefined` until the store is known, which leaves the resource idle. The store directory
   * resolves a moment after the page first renders, and without this gate the page would fire two
   * requests on every open — one unscoped, one correct.
   */
  private readonly query = computed(() => {
    const storeId = this.shell.currentStoreId();
    if (!storeId) {
      return undefined;
    }
    return {page: {page: this.pageIndex(), count: PAGE_SIZE}, search: this.search(), storeId};
  });

  private readonly customers = snapshot(
    () => this.query(),
    (query) => this.api.loadCustomers(query),
  );

  readonly isLoading = this.customers.isLoading;
  readonly error = this.customers.error;
  readonly isEmpty = this.customers.isEmpty;
  readonly reload = () => this.customers.reload();

  readonly rows = computed<readonly CustomerRow[]>(() => this.customers.value()?.rows ?? []);

  /**
   * Whether the rows on screen answer the term in the box.
   *
   * False while a new term is in flight, because the last good rows stay up in the meantime. Only
   * the auto-open reads it, and only because acting on a stale row count would be acting on the
   * previous question's answer.
   */
  readonly rowsMatchSearch = computed(() => this.customers.value()?.search === this.search());
  readonly totalElements = computed(() => this.customers.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.customers.value()?.totalPages ?? 0);

  /**
   * The open customer, found in the rows already loaded.
   *
   * TODO(lessons.md): reading one customer by id — no backend endpoint. See lessons.md, "Customers —
   * no customer detail endpoint".
   *
   * There is no `GET …/private/customers/{id}` to fall back to, so a customer who is not on the
   * current page cannot be opened — which is why a link into this page carries a search term and
   * the page opens the row when the term matches exactly one. See lessons.md, "Customers — no
   * customer detail endpoint".
   */
  readonly selected = computed<CustomerRow | null>(() => {
    const id = this.selectedId();
    return id === null ? null : (this.rows().find((row) => row.id === id) ?? null);
  });

  /**
   * The open customer's orders, on their own key.
   *
   * Gated on the selection, so nothing is fetched until a customer is opened, and re-run per
   * customer. Its failure is the panel's, not the page's: a customer record is still worth reading
   * when the orders endpoint is unreachable.
   */
  private readonly orders = snapshot(
    () => this.selectedId() ?? undefined,
    (customerId) => this.api.loadOrders(customerId),
  );

  readonly orderRows = computed<readonly CustomerOrderRow[]>(() => this.orders.value()?.rows ?? []);
  readonly ordersLoading = this.orders.isLoading;
  readonly ordersError = this.orders.error;
  readonly reloadOrders = () => this.orders.reload();

  /**
   * How many orders the open customer has placed, exactly.
   *
   * `totalElements` from the same response the panel's rows came out of — the one lifetime figure on
   * this page that is real. Money figures are not: there is no aggregate, and summing the handful on
   * screen would be a different number under the same label. See lessons.md, "Customers — no
   * lifetime or per-customer aggregate".
   */
  readonly orderCount = computed<number | null>(() => this.orders.value()?.totalElements ?? null);

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('customers.heading.title'),
      context: this.transloco.translate('customers.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
      }),
    };
  });

  /** What the table is showing right now, under the panel title. */
  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const total = this.totalElements();
    const shown = this.rows().length;
    if (!shown) {
      return this.transloco.translate('customers.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = this.pageIndex() * PAGE_SIZE + 1;
    return this.transloco.translate('customers.subtitle.range', {
      from: digits(from),
      to: digits(from + shown - 1),
      total: digits(total),
      count: total,
    });
  });

  /** The count as the header carries it, or null before the first response. */
  readonly totalLabel = computed<string | null>(() => {
    if (this.isEmpty()) {
      return null;
    }
    this.transloco.activeLang();
    return this.transloco.translate('customers.totalLabel', {
      total: this.localeFormat.localizeNumber(this.totalElements(), 'decimal'),
      count: this.totalElements(),
    });
  });

  /** An order's amount, in the reader's language. `text` is null on every total the list sends. */
  orderTotal(row: CustomerOrderRow): string {
    return this.money.format(row.total.value, row.total.currency, row.total.text);
  }

  statusLabel(status: string | undefined): string {
    return this.statusLabels.label(status);
  }

  /** Known statuses keep their categorical colour; anything new stays neutral. */
  statusTone(status: string | undefined): Tone {
    return (status && STATUS_TONE[status as keyof typeof STATUS_TONE]) || 'slate';
  }

  orderCountLabel(): string {
    this.transloco.activeLang();
    const count = this.orderCount();
    if (count === null) {
      return '—';
    }
    return this.localeFormat.localizeNumber(count, 'decimal');
  }

  selectRow(id: number): void {
    this.selectedId.set(id);
  }

  clearSelection(): void {
    this.selectedId.set(null);
  }

  setSearch(term: string): void {
    this.search.set(term);
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }
}
