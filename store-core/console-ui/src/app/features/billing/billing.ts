import {Component, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ActivatedRoute, Router} from '@angular/router';
import {map} from 'rxjs';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {PlatformLabel} from '@shared/i18n/platform-label';
import {Money} from '@shared/i18n/money';
import {PlanDialog} from '@layouts/console-shell/billing/plan-dialog/plan-dialog';
import {SectionNav, type NavSection, TabSwitcher, type TabItem, ConfirmDialog, Badge, BusyOverlay, EmptyState, Icon, LoadError, PageHeader, Panel} from '@cvhome-saas/ui-kit/ui';
import type {Tone} from '@cvhome-saas/ui-kit/ui';
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
    EmptyState,
    TabSwitcher,
    LoadError,
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
  private readonly platformLabels = inject(PlatformLabel);
  private readonly money = inject(Money);

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly billing = this.facade.billing;
  protected readonly confirmingCancel = signal(false);
  protected readonly sections = BILLING_SECTIONS;
  protected readonly railCollapsed = signal(false);

  /**
   * The same sections as a tab strip, for a viewport too narrow for the rail.
   *
   * Store management has had this since the rail was promoted to `app-section-nav`; billing grew
   * the rail and not the fallback, so below 44rem this page simply had no navigation.
   */
  protected tabs(t: (key: string) => string): readonly TabItem[] {
    return BILLING_SECTIONS.map((section) => ({key: section.key, label: t(section.labelKey)}));
  }

  protected pickSection(key: string): void {
    void this.router.navigate(['/subscription', key]);
  }

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

  /**
   * The subscription's status, in the reader's language.
   *
   * Through the shared label rather than a `billing.status.*` key of its own: the platform console's
   * organization detail renders the same enum for another tenant's stores, and one enum with two
   * sets of words drifts into two different words for the same state. It also brings the known-set
   * guard with it — a status added server-side no longer takes this page down.
   */
  protected readonly statusLabel = computed(() => {
    const status = this.billing.status();
    return status ? this.platformLabels.subscriptionStatus(status) : '';
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
