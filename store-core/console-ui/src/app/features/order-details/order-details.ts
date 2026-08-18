import {DatePipe} from '@angular/common';
import {Component, ElementRef, computed, effect, inject, input, signal, viewChild} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {OrderStatus} from '@models/checkout';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {OrderDetailsFacade} from './facades/order-details.facade';

/**
 * One order.
 *
 * The design (`console-template/Order Details.dc.html`) draws about twenty blocks; six of them have
 * data. What is here — items, totals, addresses, customer, timeline, and adding a status — is
 * everything checkout can answer. Refund, capture, cancel, duplicate, shipment, tracking, internal
 * notes and the rest are absent rather than disabled: see lessons.md.
 *
 * The invoice is the exception. Every figure on it is already on the order, so it renders and prints
 * from `ExportButton` with no backend at all.
 */
@Component({
  selector: 'app-order-details',
  imports: [
    Badge,
    BusyOverlay,
    DatePipe,
    ExportButton,
    Icon,
    PageHeader,
    Panel,
    RouterLink,
    TranslocoDirective,
  ],
  providers: [OrderDetailsFacade],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css',
})
export class OrderDetails {
  /** Bound from the route by `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly transloco = inject(TranslocoService);
  protected readonly facade = inject(OrderDetailsFacade);

  /** The status the operator is about to record, and the note that goes with it. */
  protected readonly nextStatus = signal<OrderStatus>('PROCESSING');
  protected readonly comment = signal('');

  protected readonly report = viewChild<ElementRef<HTMLElement>>('invoice');

  protected readonly title = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('orderDetails.title', {reference: this.facade.reference()});
  });

  constructor() {
    // `id` is a signal, so navigating straight from one order to another re-reads without a reload.
    effect(() => this.facade.orderId.set(Number(this.id())));
  }

  protected submitStatus(): void {
    this.facade.addStatus(this.nextStatus(), this.comment());
    this.comment.set('');
  }
}
