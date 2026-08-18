import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DestroyRef} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService} from '@core/errors/api-error.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  ORDER_STATUSES,
  formatMoney,
  isEmptyAddress,
  totalLabel,
  type CustomerAddress,
  type OrderStatus,
  type OrderTotal,
} from '@models/checkout';
import {STATUS_TONE} from '@models/orders';
import {StatusLabel} from '@shared/i18n/status-label';
import {ToastService} from '@shared/ui/toast/toast';
import {OrderDetailsApi} from '../services/order-details.api.service';

/** One address, flattened into the lines a panel prints. */
export interface AddressView {
  readonly name: string;
  readonly lines: readonly string[];
  readonly phone: string | null;
  readonly email: string | null;
}

/** The seller, as the invoice prints it in its letterhead. */
export interface SellerView {
  readonly name: string;
  /** Resolvable image path, when the store has uploaded a logo. */
  readonly logo: string | null;
  readonly lines: readonly string[];
  readonly email: string | null;
  readonly phone: string | null;
}

/**
 * One stage of the fulfilment tracker.
 *
 * Five stages rather than the ten `OrderStatus` values: an operator watching an order wants to know
 * how far it has got, not which of two synonyms for "in the warehouse" the server recorded. Each
 * stage owns the statuses that satisfy it.
 */
export interface FulfilmentStage {
  readonly labelKey: string;
  readonly state: 'done' | 'current' | 'todo';
  /** When the stage was reached, from the order's own history. Null while it is still ahead. */
  readonly reachedAt: string | null;
  /** How full the stage's bar is: 1 for reached, a half-step for the one in progress. */
  readonly fill: number;
}

export interface TimelineEntry {
  readonly status: string;
  readonly tone: ReturnType<typeof toneOf>;
  readonly comment: string | null;
  readonly date: string | null;
}

function toneOf(status: OrderStatus | undefined) {
  return status ? STATUS_TONE[status] : 'slate';
}

/**
 * The stages the tracker shows, and which statuses reach each one.
 *
 * `CANCELLED` and `RETURNED` appear in no stage on purpose: they are not points on the way to
 * delivery, they are the order leaving the path. The tracker is replaced by a notice when either is
 * the order's status, rather than showing a half-filled progress bar for an order that is not
 * progressing.
 */
const STAGES: readonly {labelKey: string; statuses: readonly OrderStatus[]}[] = [
  {labelKey: 'orderDetails.stage.placed', statuses: ['CREATED']},
  {labelKey: 'orderDetails.stage.paid', statuses: ['PENDING_PAYMENT', 'CONFIRMED']},
  {labelKey: 'orderDetails.stage.picked', statuses: ['PROCESSING']},
  {labelKey: 'orderDetails.stage.shipped', statuses: ['SHIPPED', 'DELIVERING']},
  {labelKey: 'orderDetails.stage.delivered', statuses: ['DELIVERED', 'COMPLETED']},
];

/** Where the order sits, as an index into `STAGES`. `-1` when its status is off the path. */
function stageIndexOf(status: OrderStatus | undefined): number {
  return status ? STAGES.findIndex((stage) => stage.statuses.includes(status)) : -1;
}

/**
 * The order detail screen.
 *
 * Everything on it is a reading of one `ReadableOrder` plus its history. The screen's single write is
 * adding a status change — refund, capture, cancel, duplicate, shipment and address editing all have
 * no endpoint, and are absent from the page rather than present and broken. See lessons.md.
 */
@Injectable()
export class OrderDetailsFacade {
  private readonly api = inject(OrderDetailsApi);
  private readonly transloco = inject(TranslocoService);
  private readonly toasts = inject(ToastService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly statusLabels = inject(StatusLabel);
  private readonly destroyRef = inject(DestroyRef);

  /** Set by the component from the route. */
  readonly orderId = signal<number | null>(null);

  readonly statuses = ORDER_STATUSES;
  readonly submitting = signal(false);

  private readonly detail = rxResource({
    // The store rides along because the invoice's letterhead is the selling store, and it is read by
    // path rather than by the request context's `?store=`. Switching stores re-reads both.
    //
    // Nothing is requested until the store directory has resolved: every call the page makes is
    // store-scoped, so firing early would fetch the order once unscoped and again scoped.
    params: () => {
      const orderId = this.orderId();
      const storeId = this.shell.currentStoreId();
      return orderId === null || storeId === null ? undefined : {orderId, storeId};
    },
    stream: ({params}) => this.api.load(params.orderId, params.storeId),
  });

  readonly isLoading = this.detail.isLoading;
  readonly error = computed(() => this.detail.error() as Error | undefined);
  readonly order = computed(() => (this.detail.hasValue() ? this.detail.value()?.order : undefined));
  readonly isEmpty = computed(() => this.order() === undefined);

  readonly reference = computed(() => {
    const id = this.order()?.id;
    return id === undefined ? '' : `#${id}`;
  });

  readonly status = computed(() => {
    return this.statusLabels.label(this.order()?.orderStatus);
  });

  readonly statusTone = computed(() => toneOf(this.order()?.orderStatus));

  /**
   * The buyer.
   *
   * Falls back to the billing address the way the list does — the list's `customer` is null, and a
   * detail page that disagreed with the row the operator clicked would be worse than either.
   */
  readonly customer = computed(() => {
    const order = this.order();
    const person = order?.customer ?? order?.billing;
    const name = [person?.firstName, person?.lastName].filter(Boolean).join(' ').trim();
    const email = order?.customer?.emailAddress ?? order?.billing?.email ?? null;

    return {
      name: name || email || '—',
      company: order?.billing?.company?.trim() || null,
      email,
      phone: order?.billing?.phone?.trim() || null,
      initials: initialsOf(name || email || ''),
    };
  });

  /**
   * The line under the title: when it was placed, how many lines, what it came to.
   *
   * The mockup also puts the channel and the store here. There is no channel on an order, and the
   * store is already named in the rail two inches to the left.
   */
  readonly summary = computed(() => {
    const order = this.order();
    return {
      placedAt: order?.datePurchased ?? null,
      itemCount: order?.products?.length ?? 0,
      total: formatMoney(order?.total, order?.currency),
    };
  });

  /**
   * The invoice, as a document.
   *
   * Every figure is read off the order, which is why this can exist at all with no invoice service
   * behind it — the letterhead aside, which is the selling store read from the merchant service.
   * What a real invoice service would add — a stable invoice number of its own, an issue date
   * distinct from the order date, a tax id, payment terms — is absent rather than invented. See
   * lessons.md, "Orders — no invoice service".
   */
  readonly invoice = computed(() => {
    const order = this.order();
    return {
      orderReference: this.reference(),
      issuedAt: order?.datePurchased ?? null,
      paid: order?.paymentStatus === 'PAID',
      paymentStatus: this.statusLabels.label(order?.paymentStatus),
      billing: this.billing(),
      delivery: this.delivery(),
    };
  });

  /**
   * The invoice's letterhead: the selling store as the merchant service describes it.
   *
   * Only what the store actually filled in is printed — a blank address line or a lone `undefined`
   * on an invoice reads as a defect in the seller, not in the console. When the store could not be
   * read at all, `name` still comes from the rail, which is the one thing the console always knows.
   */
  readonly seller = computed<SellerView>(() => {
    const store = this.detail.hasValue() ? this.detail.value()?.seller : undefined;
    const countries = this.detail.hasValue() ? this.detail.value()?.countries : undefined;
    const address = store?.address;
    const country = this.countryName(address?.country, countries);
    const name = store?.name?.trim() || this.shell.currentStore()?.name || '';

    return {
      name,
      logo: store?.logo?.path?.trim() || null,
      lines: [address?.address, [address?.postalCode, address?.city].filter(Boolean).join(' '), address?.stateProvince, country]
        .map((line) => line?.trim())
        .filter((line): line is string => !!line),
      email: store?.email?.trim() || null,
      phone: store?.phone?.trim() || null,
    };
  });

  readonly items = computed(() => this.order()?.products ?? []);

  /**
   * The totals block, as the server computed it.
   *
   * Rendering `totals[]` rather than picking `total`/`tax`/`shipping` off the order means a store
   * with a discount or a surcharge shows it without the console needing to know the line exists.
   *
   * Both the label and the amount are derived here: against the running stack every total arrives
   * with `title: null` and `text: null`, so the line is named from its `module` and the amount is
   * formatted from `value` and the order's currency.
   */
  readonly totals = computed(() => {
    const order = this.order();
    return (order?.totals ?? []).map((total: OrderTotal) => ({
      label: totalLabel(total),
      amount: formatMoney(total, order?.currency),
      // `order.total.total` is the grand total; the rest are lines above it.
      grand: total.module === 'total',
    }));
  });

  readonly billing = computed(() => this.addressOf(this.order()?.billing, this.order()?.billing?.email));
  readonly delivery = computed(() => this.addressOf(this.order()?.delivery, null));

  readonly flags = computed(() => {
    const order = this.order();
    if (!order) {
      return [];
    }
    return [
      {labelKey: 'orderDetails.flag.paymentStatus', value: this.statusLabels.label(order.paymentStatus)},
      {labelKey: 'orderDetails.flag.reservationStatus', value: this.statusLabels.label(order.reservationStatus)},
      {labelKey: 'orderDetails.flag.customerAgreed', value: this.yesNo(order.customerAgreed)},
      {labelKey: 'orderDetails.flag.confirmedAddress', value: this.yesNo(order.confirmedAddress)},
    ];
  });

  /** True when the order left the fulfilment path rather than stalling on it. */
  readonly isCancelled = computed(() => {
    const status = this.order()?.orderStatus;
    return status === 'CANCELLED' || status === 'RETURNED';
  });

  /**
   * The fulfilment tracker.
   *
   * Both the position and the timestamps are real: the position comes from the order's status, and
   * each reached stage is dated from the **first** history entry that put it there. Nothing here is
   * estimated — a stage still ahead simply carries no date, which is the honest answer to "when will
   * this ship" on a platform with no shipping model at all.
   */
  readonly stages = computed<readonly FulfilmentStage[]>(() => {
    const history = this.detail.hasValue() ? (this.detail.value()?.history ?? []) : [];
    const current = stageIndexOf(this.order()?.orderStatus);

    return STAGES.map((stage, index) => {
      const state = index < current ? 'done' : index === current ? 'current' : 'todo';
      const entry = history.find((it) => it.orderStatus && stage.statuses.includes(it.orderStatus));

      return {
        labelKey: stage.labelKey,
        state,
        // Only for a stage actually reached. An order can move *backwards* — one that went out for
        // delivery and came back to picking still has a history entry for shipping, and dating a
        // stage ahead of the order with it would say it had already happened.
        reachedAt: state === 'todo' ? null : (entry?.date ?? null),
        fill: state === 'done' ? 1 : state === 'current' ? 0.5 : 0,
      } satisfies FulfilmentStage;
    });
  });

  readonly timeline = computed<readonly TimelineEntry[]>(() => {
    const history = this.detail.hasValue() ? (this.detail.value()?.history ?? []) : [];
    return history.map((entry) => ({
      status: this.statusLabels.label(entry.orderStatus),
      tone: toneOf(entry.orderStatus),
      comment: entry.comments?.trim() || null,
      date: entry.date ?? null,
    }));
  });

  /** The status options, humanized. The server owns the enum, so these are never looked up. */
  readonly statusOptions = computed(() =>
    ORDER_STATUSES.map((status) => ({value: status, label: this.statusLabels.label(status)})),
  );

  /**
   * Records a status change.
   *
   * Reloads rather than pushing onto the timeline locally: the server decides what the order's status
   * becomes, and a screen that assumed would eventually disagree with it.
   */
  addStatus(status: OrderStatus, comments: string): void {
    const id = this.orderId();
    if (id === null || this.submitting()) {
      return;
    }
    this.submitting.set(true);

    this.api
      .addStatus(id, status, comments)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toasts.success(this.transloco.translate('orderDetails.statusAdded'));
          this.detail.reload();
        },
        error: (error: unknown) => {
          this.submitting.set(false);
          this.apiErrors.notify(error);
        },
      });
  }

  retry(): void {
    this.detail.reload();
  }

  /**
   * Flattens an address into printable lines, dropping the parts this one does not carry.
   *
   * Orders come from many countries and the shape of an address is not universal; joining only what
   * is present avoids the blank lines and stray commas a fixed template produces.
   */
  private addressOf(address: CustomerAddress | undefined, email: string | null | undefined): AddressView | null {
    // An order with no delivery address still carries a `delivery` object with every field null,
    // so absence has to be tested field by field rather than by the object being missing.
    if (!address || isEmptyAddress(address)) {
      return null;
    }
    const countries = this.detail.hasValue() ? this.detail.value()?.countries : undefined;
    const country = this.countryName(address.country, countries);

    return {
      name: [address.firstName, address.lastName].filter(Boolean).join(' ').trim() || '—',
      lines: [
        address.company,
        address.address,
        [address.postalCode, address.city].filter(Boolean).join(' '),
        address.stateProvince ?? address.zone,
        country,
      ]
        .map((line) => line?.trim())
        .filter((line): line is string => !!line),
      phone: address.phone ?? null,
      email: email ?? null,
    };
  }

  /**
   * An ISO country code as a name.
   *
   * Checkout's own list answers first, because that is the platform's truth. It is short — the store
   * this was written against supports four countries — and an order or a store address may name one
   * outside it, so `Intl.DisplayNames` resolves the rest from the code itself. That is formatting, not
   * invented data: the code is what the server sent, and the browser is only spelling it out, in the
   * reader's language. An unknown code falls through to itself rather than disappearing.
   */
  private countryName(code: string | undefined, countries: ReadonlyMap<string, string> | undefined): string | null {
    if (!code) {
      return null;
    }
    const known = countries?.get(code);
    if (known) {
      return known;
    }
    try {
      const names = new Intl.DisplayNames([this.transloco.getActiveLang()], {type: 'region'});
      return names.of(code) ?? code;
    } catch {
      return code;
    }
  }

  private yesNo(value: boolean | undefined): string {
    this.transloco.activeLang();
    return this.transloco.translate(value ? 'orderDetails.yes' : 'orderDetails.no');
  }
}

/** First letters of the first two words, for the avatar tile. */
function initialsOf(name: string): string {
  return (
    name
      .split(/[\s@.]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((word) => word.charAt(0))
      .join('')
      .toUpperCase() || '?'
  );
}
