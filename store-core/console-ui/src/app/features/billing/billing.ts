import {Component, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ActivatedRoute} from '@angular/router';
import {map} from 'rxjs';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Money} from '@shared/i18n/money';
import {PlanDialog} from '@layouts/console-shell/billing/plan-dialog/plan-dialog';
import {SectionNav, type NavSection} from '@shared/ui/section-nav/section-nav';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Icon} from '@shared/ui/icon/icon';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import type {Tone} from '@shared/ui/tone';
import type {SubscriptionStatus} from '@models/billing';
import {BillingPageFacade} from './facades/billing-page.facade';

/** The rail's sections, in reading order: what you are on, then what you were charged. */
const BILLING_SECTIONS: readonly NavSection[] = [
  {key: 'plan', labelKey: 'billing.section.plan', icon: 'creditCard'},
  {key: 'invoices', labelKey: 'billing.section.invoices', icon: 'receipt'},
];

/** How each subscription status is coloured. Only `ACTIVE` and `TRIALING` are good news. */
const STATUS_TONE: Record<SubscriptionStatus, Tone> = {
  ACTIVE: 'green',
  TRIALING: 'green',
  PENDING: 'amber',
  PAST_DUE: 'amber',
  SUSPENDED: 'red',
  CANCELED: 'red',
};

/**
 * Billing for the store the console has open: what it is on, what that grants, and its invoices.
 *
 * Every panel is billing's own answer — `subscription/current`, its entitlement map, and
 * `invoice/list`. Nothing on this page is authored, and the plan catalogue behind *Change plan* is
 * the same one the marketing page prices from.
 *
 * The page deliberately does **not** render an invoice. Stripe's hosted copy is the one that is
 * legally the invoice, so each row links out to it and to its PDF rather than reproducing figures the
 * console would then have to keep correct.
 */
@Component({
  selector: 'app-billing',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    Icon,
    PageHeader,
    Panel,
    PlanDialog,
    SectionNav,
    TranslocoDirective,
  ],
  providers: [BillingPageFacade],
  templateUrl: './billing.html',
  styleUrl: './billing.css',
})
export class Billing {
  protected readonly facade = inject(BillingPageFacade);
  private readonly transloco = inject(TranslocoService);
  private readonly money = inject(Money);

  private readonly route = inject(ActivatedRoute);

  protected readonly billing = this.facade.billing;
  protected readonly confirmingCancel = signal(false);
  protected readonly sections = BILLING_SECTIONS;
  protected readonly railCollapsed = signal(false);

  /**
   * The open section, from the URL.
   *
   * Falls back to `plan` for an unknown segment rather than showing an empty page: the rail is the
   * only thing that writes these, but a stale bookmark or a typed URL is not the operator's fault.
   */
  protected readonly section = toSignal(
    this.route.paramMap.pipe(
      map((params) => params.get('section') ?? 'plan'),
      map((key) => (BILLING_SECTIONS.some((entry) => entry.key === key) ? key : 'plan')),
    ),
    {initialValue: 'plan'},
  );

  protected readonly heading = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('billing.title');
  });

  /**
   * The context line under the title.
   *
   * Keyed off the *plan*, not the subscription: a row can exist with no plan attached — a store whose
   * provisioning event has landed but whose plan has not — and "This store is on —" is not a sentence.
   */
  protected readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const planCode = this.billing.subscription()?.planCode;
    return planCode
      ? this.transloco.translate('billing.subtitle.on', {plan: this.billing.planName()})
      : this.transloco.translate('billing.subtitle.none');
  });

  protected readonly statusTone = computed<Tone>(() => {
    const status = this.billing.status();
    return status ? STATUS_TONE[status] : 'slate';
  });

  protected readonly statusLabel = computed(() => {
    this.transloco.activeLang();
    const status = this.billing.status();
    return status ? this.transloco.translate(`billing.status.${status}`) : '';
  });

  /**
   * Money as billing sends it — minor units and an ISO code — written in the reader's locale.
   *
   * `Money` rather than Angular's `CurrencyPipe`: the pipe formats against `LOCALE_ID`, which is
   * fixed at bootstrap and stays `en-US` however the operator switches the console. `Money` reads
   * the active language, so switching to Arabic re-renders the figure in Arabic-Indic numerals.
   *
   * `account` rather than `format`, so the currency is an ISO code rather than a symbol — see the
   * service for why `US$` in an Arabic line is the wrong answer.
   */
  protected amount(minorUnits: number, currency: string): string {
    return this.money.account(minorUnits / 100, currency);
  }

  protected confirmCancel(): void {
    this.confirmingCancel.set(false);
    this.facade.cancel();
  }
}
