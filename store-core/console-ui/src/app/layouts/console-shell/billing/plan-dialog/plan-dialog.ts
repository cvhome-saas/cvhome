import {
  Component,
  ElementRef,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoDirective} from '@jsverse/transloco';

import {SubscriptionService} from '@api/billing/subscription.service';
import {Money} from '@shared/i18n/money';
import {toPricingPlans} from '@shared/billing/pricing.mapper';
import type {BillingInterval} from '@models/billing';
import type {PricingPlan} from '@models/marketing';
import {PlanLabel} from '@shared/billing/plan-label';

/**
 * The plan catalog, compared, for a signed-in operator.
 *
 * The console's trial notice offered "View plans" and had nowhere to send anyone — it raised a
 * "not available yet" toast, while `GET /billing/api/v1/plan/public/plans` had been serving the
 * real catalog to the marketing page the whole time. This is that same catalog, in the console,
 * behind a dialog rather than a page: comparing plans is something an operator does *while*
 * doing something else, and a route would lose whatever they were on.
 *
 * Prices, ceilings, trial lengths and the yearly saving all come from billing. Nothing here is
 * authored, and the mapper is the one the marketing page uses, so the two can no longer disagree
 * about what a plan costs or grants.
 *
 * Choosing a plan is deliberately **not** wired: `POST` checkout is store-scoped and there is no
 * store yet on the surface that opens this. The dialog reports the catalog and emits `chosen` for
 * a caller that has somewhere to take it.
 */
@Component({
  selector: 'app-plan-dialog',
  imports: [TranslocoDirective],
  templateUrl: './plan-dialog.html',
  styleUrl: './plan-dialog.css',
})
export class PlanDialog {
  private readonly subscriptions = inject(SubscriptionService);
  private readonly money = inject(Money);
  private readonly planLabels = inject(PlanLabel);

  readonly open = input(false);
  /** Restricts the catalog to one currency when the caller knows the operator's. */
  readonly currency = input<string | null>(null);
  /** Emitted with a `plan_price` id. Absent a handler the dialog is read-only, which is the default. */
  readonly chosen = output<string>();
  readonly closed = output<void>();

  /**
   * Optional, not `required`, and the reason is structural: this template is wrapped in
   * `*transloco`, so the dialog lives in an embedded view that does not exist until translations
   * resolve. A `required` query throws NG0951 the first time an effect reads it. As a plain signal
   * it simply reports "not yet", and the effect re-runs when the view appears — which is also what
   * makes a dialog opened before that point still open.
   */
  private readonly dialog = viewChild<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly interval = signal<BillingInterval>('MONTH');

  /**
   * Loaded once, lazily, and kept.
   *
   * `rxResource` runs when the dialog first opens rather than with the page: the catalog is a
   * request nobody has asked for until someone wants to compare plans, and the surfaces that host
   * this dialog render fine without it.
   */
  private readonly catalog = rxResource({
    params: () => (this.open() ? {currency: this.currency()} : undefined),
    stream: ({params}) => this.subscriptions.plans(params.currency ?? undefined),
  });

  protected readonly isLoading = this.catalog.isLoading;
  protected readonly failed = computed(() => this.catalog.error() !== undefined);

  protected readonly plans = computed<readonly PricingPlan[]>(() =>
    toPricingPlans(this.catalog.value() ?? [], this.interval()),
  );

  /** Nothing to compare is a different statement from a failed load, and reads differently. */
  protected readonly isEmpty = computed(
    () => !this.isLoading() && !this.failed() && this.plans().length === 0,
  );

  protected readonly yearly = computed(() => this.interval() === 'YEAR');

  /** A price in the reader's locale, currency as an ISO code — see `Money.account`. */
  protected price(plan: PricingPlan): string {
    return this.money.account(plan.amount, plan.currency);
  }

  /** The plan's name in the reader's language, falling back to the catalogue's own. */
  protected name(plan: PricingPlan): string {
    return this.planLabels.of(plan.code, plan.name);
  }

  constructor() {
    effect(() => {
      const element = this.dialog()?.nativeElement;
      if (!element) {
        return;
      }
      if (this.open()) {
        if (!element.open) {
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected setInterval(interval: BillingInterval): void {
    this.interval.set(interval);
  }

  protected choose(priceId: string): void {
    this.chosen.emit(priceId);
  }

  protected close(): void {
    this.dialog()?.nativeElement.close();
  }

  protected retry(): void {
    this.catalog.reload();
  }
}
