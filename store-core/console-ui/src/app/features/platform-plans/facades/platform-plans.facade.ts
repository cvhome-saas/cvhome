import {Injectable, computed, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ENTITLEMENT_ORDER, type EntitlementKey, type EntitlementValue, type PlanView} from '@models/billing';
import {Money} from '@shared/i18n/money';
import {snapshot} from '@shared/state/snapshot';
import {PlatformPlansApi} from '../services/platform-plans.api.service';

/** Minor units to major, the catalogue's own convention. */
const MINOR_UNITS = 100;

/** One plan, as the reference table renders it. */
export interface PlanRow {
  readonly code: string;
  readonly name: string;
  readonly description: string;
  readonly tier: number;
  /** The monthly price, written in the reader's locale, or an em dash where the plan has none. */
  readonly monthly: string;
  readonly yearly: string;
  /** The trial the monthly price grants, in days. Zero where there is none. */
  readonly trialDays: number;
  /**
   * How many stores are on this plan right now, written in the reader's locale.
   *
   * An em dash when the plan statistics could not be read — that leg is optional, so losing it costs
   * two columns rather than the catalogue. Zero and unknown are different answers: a plan nobody has
   * bought is a commercial fact, and a figure the console could not fetch is not.
   */
  readonly subscribers: string;
  /** What those subscriptions are contracted to bring in, annualised, per currency. */
  readonly recurring: string;
  /** One cell per row of {@link PlatformPlansFacade.entitlementRows}, in the same order. */
  readonly allowances: readonly string[];
}

/**
 * What the platform sells, and what that is actually worth.
 *
 * **A price list until billing grew an aggregate.** This screen was once the only platform-wide read
 * billing would answer at all — every other endpoint was store-scoped and had no super-admin branch,
 * so an operator could see what the platform *charged* and never what it earned. The subscriber
 * count and the run rate on each row come from `plan-statistic`, and they are what turn a price list
 * into a commercial reading.
 *
 * **The counts leg is optional and the catalogue is not.** Losing the statistics costs two columns;
 * losing the catalogue is the page, so it reaches the error state with a retry.
 *
 * Still read-only. Plans are created in Stripe and mirrored into `billing.plan`; nothing on the
 * platform exposes a write, and inventing one here would be inventing a second source of truth for
 * what a customer is charged. What happens to those plans — who moved onto one, who stopped paying —
 * is `/platform/billing`.
 */
@Injectable()
export class PlatformPlansFacade {
  private readonly api = inject(PlatformPlansApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly money = inject(Money);

  private readonly catalogue = snapshot(
    // No currency: the endpoint's parameter filters the catalogue to one, and an operator wants to
    // see everything the platform sells rather than one market's slice of it.
    () => true,
    () => this.api.loadCatalogue(),
  );

  readonly isLoading = this.catalogue.isLoading;
  readonly error = this.catalogue.error;
  readonly isEmpty = this.catalogue.isEmpty;
  readonly reload = () => this.catalogue.reload();

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.plans.heading.title'),
      context: this.transloco.translate('platform.plans.heading.context'),
    };
  });

  /**
   * The entitlement rows, in the catalogue's fixed order.
   *
   * Walked from `ENTITLEMENT_ORDER` rather than from the response, because **an omitted key means
   * unlimited** — reading the keys a plan mentions would silently drop exactly the ones worth
   * showing, and rank the most generous plan as the barest.
   */
  readonly entitlementRows = computed<readonly {key: EntitlementKey; label: string}[]>(() => {
    this.transloco.activeLang();
    return ENTITLEMENT_ORDER.map((key) => ({
      key,
      label: this.transloco.translate(`shared.entitlement.${key}.granted`),
    }));
  });

  readonly rows = computed<readonly PlanRow[]>(() => {
    this.transloco.activeLang();
    const plans = this.catalogue.value()?.plans ?? [];
    return [...plans].sort((a, b) => a.tier - b.tier).map((plan) => this.toRow(plan));
  });

  /** Whether the statistics leg answered. False leaves the two commercial columns showing em dashes. */
  readonly hasStatistics = computed(() => !!this.catalogue.value()?.statistics);

  private toRow(plan: PlanView): PlanRow {
    const monthly = plan.prices.find((price) => price.interval === 'MONTH');
    const yearly = plan.prices.find((price) => price.interval === 'YEAR');
    return {
      code: plan.code,
      name: plan.displayName,
      description: plan.description ?? '',
      tier: plan.tier,
      monthly: this.price(plan, 'MONTH'),
      yearly: this.price(plan, 'YEAR'),
      trialDays: monthly?.trialDays ?? yearly?.trialDays ?? 0,
      subscribers: this.subscribersOf(plan.code),
      recurring: this.recurringOf(plan.code),
      allowances: ENTITLEMENT_ORDER.map((key) => this.allowance(key, plan.entitlements[key])),
    };
  }

  /**
   * How many stores are on this plan, across every lifecycle state.
   *
   * Every state, not only `ACTIVE`: the column answers "who is on this plan", and a trialling store
   * is on it. The run-rate column beside it is the one that counts only what is committed, and its
   * header says so — splitting the question that way is what keeps a book from being overstated by a
   * column that looks like a count.
   */
  private subscribersOf(code: string): string {
    const statistics = this.catalogue.value()?.statistics;
    if (!statistics) {
      return '—';
    }
    const total = statistics.counts
      .filter((count) => count.planCode === code)
      .reduce((sum, count) => sum + count.subscriptions, 0);
    return this.localeFormat.localizeNumber(total, 'decimal');
  }

  /**
   * The committed annual value of this plan, one figure per currency.
   *
   * `ACTIVE` only, and already annualised by the server — nothing here divides, because dividing a
   * yearly price by twelve truncates on every row. Two currencies are written as two figures rather
   * than added: nothing on the platform holds an exchange rate.
   */
  private recurringOf(code: string): string {
    const statistics = this.catalogue.value()?.statistics;
    if (!statistics) {
      return '—';
    }
    const entries = statistics.recurringValue.filter(
      (entry) => entry.planCode === code && entry.status === 'ACTIVE' && entry.annual,
    );
    if (!entries.length) {
      return this.money.account(0, null);
    }
    return entries
      .map((entry) =>
        this.money.account(
          (entry.annual?.minorUnits ?? 0) / MINOR_UNITS,
          entry.annual?.currency.code ?? null,
        ),
      )
      .join(' · ');
  }

  /**
   * A price at one interval, or an em dash where the plan is not sold at it.
   *
   * An em dash rather than a zero: the FREE plan is monthly-only, and "€0 a year" is a different and
   * wrong claim from "not sold yearly". `minorUnits / 100` is the catalogue's own convention, as in
   * `@shared/billing/pricing.mapper`.
   */
  private price(plan: PlanView, interval: 'MONTH' | 'YEAR'): string {
    const price = plan.prices.find((it) => it.interval === interval);
    if (!price) {
      return '—';
    }
    return this.money.format(price.amount.minorUnits / 100, price.amount.currency.code);
  }

  /**
   * One entitlement, as a cell.
   *
   * The four cases the catalogue can express, and the first is the one that is easy to get backwards:
   * an **absent** key is unlimited, not withheld.
   *
   * `MAX_STORAGE_MB` carries its unit, and is the only key that needs to. The shared labels are the
   * short `granted` nouns — "Media storage" — so a bare `500` beside one is a figure a reader cannot
   * size. Every other ceiling counts a thing the column already names.
   */
  private allowance(key: EntitlementKey, value: EntitlementValue | undefined): string {
    if (value === undefined || (value.flagValue === null && value.limitValue === null)) {
      return this.transloco.translate('shared.allowance.unlimited');
    }
    if (value.flagValue !== null) {
      return this.transloco.translate(value.flagValue ? 'shared.allowance.included' : 'shared.allowance.notIncluded');
    }
    const limit = this.localeFormat.localizeNumber(value.limitValue ?? 0, 'decimal');
    return key === 'MAX_STORAGE_MB' ? this.transloco.translate('shared.allowance.megabytes', {size: limit}) : limit;
  }
}
