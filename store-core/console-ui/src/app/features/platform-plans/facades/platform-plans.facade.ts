import {Injectable, computed, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {SubscriptionService} from '@api/billing/subscription.service';
import {ENTITLEMENT_ORDER, type EntitlementKey, type EntitlementValue, type PlanView} from '@models/billing';
import {Money} from '@shared/i18n/money';
import {snapshot} from '@shared/state/snapshot';

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
  /** One cell per row of {@link PlatformPlansFacade.entitlementRows}, in the same order. */
  readonly allowances: readonly string[];
}

/**
 * What the platform sells.
 *
 * **The only platform-wide thing billing will answer.** Every other endpoint it has is store-scoped
 * and checks `hasReadAccessOnStore`, which admits an org admin, a store admin, a store moderator and
 * a service principal — and has no super-admin branch. So an operator can read no store's
 * subscription and no store's invoices; the catalogue, which is public, is the whole surface. See
 * lessons.md, "Platform — a store's subscription cannot be read by an operator".
 *
 * Read-only. Plans are created in Stripe and mirrored into `billing.plan`; nothing on the platform
 * exposes a write, and inventing one here would be inventing a second source of truth for what a
 * customer is charged.
 */
@Injectable()
export class PlatformPlansFacade {
  private readonly subscriptions = inject(SubscriptionService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly money = inject(Money);

  private readonly catalogue = snapshot(
    // No currency: the endpoint's parameter filters the catalogue to one, and an operator wants to
    // see everything the platform sells rather than one market's slice of it.
    () => true,
    () => this.subscriptions.plans(),
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
    const plans = this.catalogue.value() ?? [];
    return [...plans].sort((a, b) => a.tier - b.tier).map((plan) => this.toRow(plan));
  });

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
      allowances: ENTITLEMENT_ORDER.map((key) => this.allowance(key, plan.entitlements[key])),
    };
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
