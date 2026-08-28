import {A11yModule} from '@angular/cdk/a11y';
import {Component, ElementRef, computed, effect, inject, input, linkedSignal, signal, viewChild} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe, TranslocoDecimalPipe, TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {OrderStatus} from '@models/checkout';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {LoadError} from '@shared/ui/load-error/load-error';
import {Select} from '@shared/ui/select/select';
import {TextareaField} from '@shared/ui/textarea-field/textarea-field';
import {Panel} from '@shared/ui/panel/panel';
import {PdfExportService} from '@core/export/pdf-export.service';
import {ToastService} from '@shared/ui/toast/toast';
import {OrderDetailsFacade} from './facades/order-details.facade';
import {positiveIntParam} from '@core/routing/route-params';

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
  imports: [
    EmptyState,
    LoadError,
    Select,
    TextareaField,A11yModule, Badge, BusyOverlay, Icon, Panel, RouterLink, TranslocoDatePipe, TranslocoDecimalPipe, TranslocoDirective],
  providers: [OrderDetailsFacade],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css',
})
export class OrderDetails {
  /** Bound from the route by `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly pdf = inject(PdfExportService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly facade = inject(OrderDetailsFacade);

  /** The status the operator is about to record, and the note that goes with it. */
  protected readonly nextStatus = signal<OrderStatus>('PROCESSING');
  protected readonly comment = signal('');

  /**
   * Set when the store's logo will not load.
   *
   * Store logos are absolute URLs into object storage, and one that 404s or points at a host the
   * browser cannot reach would print a broken-image glyph on the invoice. The lettermark takes over
   * instead — the letterhead still reads as the store's. Reset by a change of logo: a different
   * store deserves its own chance to load.
   */
  protected readonly logoBroken = linkedSignal({
    source: () => this.facade.seller().logo,
    computation: () => false,
  });

  /** The invoice document, over the page. */
  protected readonly invoiceOpen = signal(false);
  protected readonly exporting = signal(false);

  // Explicitly the element — see the note in `products.ts`.
  protected readonly invoiceSheet = viewChild('invoiceSheet', {read: ElementRef});

  /** The operator composing the status note, for the composer's avatar. */
  protected readonly operator = computed(() => this.shell.user()?.initials ?? '');

  /**
   * "Placed 18 Aug 2026, 23:20", localised.
   *
   * Built here rather than in the template: the date has to be formatted *before* it is
   * interpolated into the sentence, and a pipe's options object inside a translation's parameter
   * object is two levels of braces the template parser reads as a control block.
   */
  protected readonly placedLine = computed(() => {
    const placedAt = this.facade.summary().placedAt;
    if (!placedAt) {
      return '';
    }
    this.transloco.activeLang();
    return this.transloco.translate('orderDetails.placedOn', {
      date: this.localeFormat.localizeDate(placedAt, undefined, {dateStyle: 'medium', timeStyle: 'short'}),
    });
  });

  protected readonly title = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('orderDetails.title', {reference: this.facade.reference()});
  });

  /**
   * The route's `:id`, as an order id — or null when it is not one.
   *
   * `/orders/abc` used to reach the server as `orders/NaN`, which comes back a 500 and reads to the
   * operator as "the order failed to load" rather than "there is no such order". A reference the
   * console cannot even parse is answered here, without a request.
   */
  protected readonly orderId = positiveIntParam(this.id);

  constructor() {
    // `id` is a signal, so navigating straight from one order to another re-reads without a reload.
    effect(() => this.facade.orderId.set(this.orderId()));
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
        // The sheet is already a page — one portrait A4, no console header over the seller's own.
        layout: 'document',
      });
    } catch {
      this.toast.danger(this.transloco.translate('orderDetails.invoiceFailed'));
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
   * The lifetime figures the mockup puts under the customer. Labels only — see the template.
   *
   * TODO(lessons.md): customer lifetime figures — no backend endpoint. See lessons.md,
   * "Orders — no customer analytics".
   */
  protected readonly customerStats = ['orders', 'spent', 'returns'] as const;

  /**
   * TODO(lessons.md): emailing an invoice — there is no mail service and no invoice record to
   * send. See lessons.md, "Orders — no invoice service".
   */
  protected emailInvoice(): void {
    this.toast.info(this.transloco.translate('orderDetails.emailNotAvailable'));
  }

  /**
   * The buyer, on the customers page.
   *
   * By search term rather than by id, because there is no `GET …/private/customers/{id}` to link
   * to: the customers page filters on the email and opens the record when exactly one comes back.
   * The order carries the customer's id — the detail endpoint populates `customer` where the list
   * does not — and it is of no use here for that reason. See lessons.md, "Customers — no customer
   * detail endpoint".
   *
   * Without an email there is nothing to search on, so the button says so rather than opening an
   * unfiltered list of everyone.
   */
  protected viewProfile(): void {
    const email = this.facade.customer().email;
    if (!email) {
      this.toast.info(this.transloco.translate('orderDetails.profileNeedsEmail'));
      return;
    }
    void this.router.navigate(['/customers'], {queryParams: {q: email}});
  }
}
