import {DatePipe} from '@angular/common';
import {A11yModule} from '@angular/cdk/a11y';
import {Component, ElementRef, computed, effect, inject, input, signal, viewChild} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {OrderStatus} from '@models/checkout';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {PdfExportService} from '@core/export/pdf-export.service';
import {ToastService} from '@shared/ui/toast/toast';
import {OrderDetailsFacade} from './facades/order-details.facade';

/**
 * One order.
 *
 * Follows `console-template/Order Details.dc.html`: a back control and status beside the title, a
 * fulfilment tracker across the top, items and history down the main column, and customer, addresses
 * and status in the rail. The invoice opens as a document over the page rather than as another panel
 * in it — it is a thing you send someone, not a section you scroll past.
 *
 * What the mockup draws and this omits — refund, capture, cancel, duplicate, shipment, tracking,
 * internal notes, customer lifetime value — has no endpoint. Those are absent rather than disabled;
 * see lessons.md.
 */
@Component({
  selector: 'app-order-details',
  imports: [A11yModule, Badge, BusyOverlay, DatePipe, Icon, Panel, RouterLink, TranslocoDirective],
  providers: [OrderDetailsFacade],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css',
})
export class OrderDetails {
  /** Bound from the route by `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly pdf = inject(PdfExportService);
  private readonly toasts = inject(ToastService);

  protected readonly facade = inject(OrderDetailsFacade);

  /** The status the operator is about to record, and the note that goes with it. */
  protected readonly nextStatus = signal<OrderStatus>('PROCESSING');
  protected readonly comment = signal('');

  /** The invoice document, over the page. */
  protected readonly invoiceOpen = signal(false);
  protected readonly exporting = signal(false);

  protected readonly invoiceSheet = viewChild<ElementRef<HTMLElement>>('invoiceSheet');

  /** The seller, as the invoice's letterhead. The only part of it the console actually knows. */
  protected readonly storeName = computed(() => this.shell.currentStore()?.name ?? '');

  /** The operator composing the status note, for the composer's avatar. */
  protected readonly operator = computed(() => this.shell.user()?.initials ?? '');

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

  protected openInvoice(): void {
    this.invoiceOpen.set(true);
  }

  protected closeInvoice(): void {
    this.invoiceOpen.set(false);
  }

  /** Renders the invoice sheet itself, not the page around it. */
  protected async downloadInvoice(): Promise<void> {
    const sheet = this.invoiceSheet()?.nativeElement;
    if (!sheet || this.exporting()) {
      return;
    }
    this.exporting.set(true);
    try {
      await this.pdf.export({
        element: sheet,
        fileName: `invoice-${this.facade.reference().replace('#', '')}`,
        title: this.transloco.translate('orderDetails.invoice'),
        subtitle: this.facade.reference(),
      });
    } catch {
      this.toasts.danger(this.transloco.translate('orderDetails.invoiceFailed'));
    } finally {
      this.exporting.set(false);
    }
  }

  protected printInvoice(): void {
    // The document is already laid out at page proportions, so the browser's own dialog is the
    // right tool — a second rendering path would be a second thing to keep in step.
    this.shell.closeMenus();
    window.print();
  }

  /**
   * TODO(lessons.md): a customer profile — no screen and no per-customer aggregate exist yet. See
   * lessons.md, "Orders — no customer analytics".
   */
  protected viewProfile(): void {
    this.toasts.info(this.transloco.translate('orderDetails.profileNotAvailable'));
  }
}
