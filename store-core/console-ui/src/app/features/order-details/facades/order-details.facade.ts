import {computed, inject, Injectable, signal} from '@angular/core';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DestroyRef} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService} from '@core/errors/api-error.service';
import {
  ORDER_STATUSES,
  formatMoney,
  isEmptyAddress,
  totalLabel,
  type CustomerAddress,
  type OrderStatus,
  type OrderTotal,
} from '@models/checkout';
import {STATUS_TONE, orderStatusLabel} from '@models/orders';
import {ToastService} from '@shared/ui/toast/toast';
import {OrderDetailsApi} from '../services/order-details.api.service';

/** One address, flattened into the lines a panel prints. */
export interface AddressView {
  readonly name: string;
  readonly lines: readonly string[];
  readonly phone: string | null;
  readonly email: string | null;
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
  private readonly destroyRef = inject(DestroyRef);

  /** Set by the component from the route. */
  readonly orderId = signal<number | null>(null);

  readonly statuses = ORDER_STATUSES;
  readonly submitting = signal(false);

  private readonly detail = rxResource({
    params: () => this.orderId() ?? undefined,
    stream: ({params}) => this.api.load(params),
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
    const status = this.order()?.orderStatus;
    return status ? orderStatusLabel(status) : '—';
  });

  readonly statusTone = computed(() => toneOf(this.order()?.orderStatus));

  readonly customer = computed(() => {
    const customer = this.order()?.customer;
    const name = [customer?.firstName, customer?.lastName].filter(Boolean).join(' ').trim();
    return {
      name: name || customer?.emailAddress || '—',
      email: customer?.emailAddress ?? null,
      username: customer?.username ?? null,
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
      {labelKey: 'orderDetails.flag.paymentStatus', value: order.paymentStatus ? orderStatusLabel(order.paymentStatus) : '—'},
      {labelKey: 'orderDetails.flag.reservationStatus', value: order.reservationStatus ? orderStatusLabel(order.reservationStatus) : '—'},
      {labelKey: 'orderDetails.flag.customerAgreed', value: this.yesNo(order.customerAgreed)},
      {labelKey: 'orderDetails.flag.confirmedAddress', value: this.yesNo(order.confirmedAddress)},
    ];
  });

  readonly timeline = computed<readonly TimelineEntry[]>(() => {
    const history = this.detail.hasValue() ? (this.detail.value()?.history ?? []) : [];
    return history.map((entry) => ({
      status: entry.orderStatus ? orderStatusLabel(entry.orderStatus) : '—',
      tone: toneOf(entry.orderStatus),
      comment: entry.comments?.trim() || null,
      date: entry.date ?? null,
    }));
  });

  /** The status options, humanized. The server owns the enum, so these are never looked up. */
  readonly statusOptions = computed(() =>
    ORDER_STATUSES.map((status) => ({value: status, label: orderStatusLabel(status)})),
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
    const country = address.country ? (countries?.get(address.country) ?? address.country) : null;

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

  private yesNo(value: boolean | undefined): string {
    this.transloco.activeLang();
    return this.transloco.translate(value ? 'orderDetails.yes' : 'orderDetails.no');
  }
}
