import {PLATFORM_ID, Injectable, Signal, WritableSignal, computed, inject, signal} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {TranslocoService} from '@jsverse/transloco';

import type {BillingInterval, PlanView} from '@models/billing';
import {ContactTopicId, PlanFeature} from '@models/marketing';
import {
  CONTACT_MESSAGE_HINT_KEYS,
  CONTACT_TOPICS,
  MARKETING_CHANNELS,
  MARKETING_METRICS,
  MARKETING_PILLARS,
  MARKETING_REVIEWS,
  MARKETING_REVIEW_STATS,
  MARKETING_STORES,
} from '../marketing.content';
import {toPricingPlans} from '@shared/billing/pricing.mapper';
import {MarketingApi} from '../services/marketing.api.service';

/** The five slots of a review card's rating row, so the template can draw filled and empty stars. */
export const RATING_SLOTS: readonly number[] = [1, 2, 3, 4, 5];

@Injectable()
export class MarketingFacade {
  private readonly api = inject(MarketingApi);
  private readonly transloco = inject(TranslocoService);
  private readonly platformId = inject(PLATFORM_ID);

  /** The catalog as billing returned it; null until it arrives, which is also the state SSR renders in. */
  private readonly catalog = signal<readonly PlanView[] | null>(null);

  readonly plansFailed = signal(false);
  readonly yearlyBilling = signal(false);
  readonly menuOpen = signal(false);
  readonly selectedTopic: WritableSignal<ContactTopicId> = signal(CONTACT_TOPICS[0].id);
  /**
   * TODO(lessons.md): contact form — no backend endpoint exists. See lessons.md,
   * "Marketing — contact form has no endpoint".
   *
   * A constant rather than a signal because there is nothing to toggle: the form is composed and validated, but
   * there is nowhere to post it. seller-ui's version calls `form.reset({})` and reports success, which loses the
   * message silently; this says so instead and points at the channels above, which are real.
   */
  readonly contactSubmitAvailable = false;

  readonly ratingSlots = RATING_SLOTS;

  readonly metrics = computed(() => {
    this.transloco.activeLang();
    return MARKETING_METRICS.map((metric) => ({
      value: metric.value,
      label: this.transloco.translate(metric.labelKey),
    }));
  });

  readonly pillars = computed(() => {
    this.transloco.activeLang();
    return MARKETING_PILLARS.map((pillar) => ({
      title: this.transloco.translate(pillar.titleKey),
      copy: this.transloco.translate(pillar.copyKey),
    }));
  });

  readonly stores = computed(() => {
    this.transloco.activeLang();
    return MARKETING_STORES.map((store) => ({
      name: store.name,
      trade: this.transloco.translate(store.tradeKey),
      market: store.market,
      mark: store.mark,
      tone: store.tone,
      since: this.transloco.translate('marketing.store.since', {year: store.since}),
    }));
  });

  readonly reviews = computed(() => {
    this.transloco.activeLang();
    return MARKETING_REVIEWS.map((review) => ({
      name: review.name,
      role: this.transloco.translate(review.roleKey),
      market: this.transloco.translate(review.marketKey),
      initials: review.initials,
      rating: review.rating,
      quote: this.transloco.translate(review.quoteKey),
    }));
  });

  readonly reviewStats = computed(() => {
    this.transloco.activeLang();
    return MARKETING_REVIEW_STATS.map((stat) => ({
      value: stat.value,
      label: this.transloco.translate(stat.labelKey),
    }));
  });

  readonly plansLoaded = computed(() => this.catalog() !== null);

  readonly plans = computed(() => {
    this.transloco.activeLang();
    const catalog = this.catalog();
    if (!catalog) {
      return [];
    }
    const interval: BillingInterval = this.yearlyBilling() ? 'YEAR' : 'MONTH';

    return toPricingPlans(catalog, interval).map((plan) => ({
      ...plan,
      features: plan.features.map((feature) => this.featureLabel(feature)),
      action: this.actionLabel(plan.free, plan.trialDays),
      saving:
        plan.savingPercent === null
          ? null
          : this.transloco.translate('marketing.pricing.saving', {percent: plan.savingPercent}),
    }));
  });

  readonly channels = computed(() => {
    this.transloco.activeLang();
    return MARKETING_CHANNELS.map((channel) => ({
      icon: channel.icon,
      title: this.transloco.translate(channel.titleKey),
      detail: this.transloco.translate(channel.detailKey),
      value: channel.value,
      href: channel.href,
    }));
  });

  readonly topics = computed(() => {
    this.transloco.activeLang();
    return CONTACT_TOPICS.map((topic) => ({
      id: topic.id,
      label: this.transloco.translate(topic.labelKey),
    }));
  });

  readonly messageHint: Signal<string> = computed(() => {
    this.transloco.activeLang();
    const key = CONTACT_MESSAGE_HINT_KEYS[this.selectedTopic()];
    return this.transloco.translate(key);
  });

  /**
   * Loads the plan catalog. Browser-only and called from the component, not the constructor: `/` is prerendered, and
   * `SelectedStoreRequestContext.params()` throws on the server by design. The section renders its full layout
   * without plans and fills them in on the client.
   */
  loadPlans(): void {
    if (!isPlatformBrowser(this.platformId) || this.catalog() !== null) {
      return;
    }
    this.api.plans().subscribe({
      next: (plans) => this.catalog.set(plans),
      // Swallowed rather than toasted: a visitor reading a landing page cannot act on a billing outage, and the
      // rest of the page is still worth reading. The section says the prices are unavailable instead of showing none.
      error: () => this.plansFailed.set(true),
    });
  }

  /** One feature line — a ceiling with its number, an unlimited ceiling, or a plain capability. */
  private featureLabel(feature: PlanFeature): string {
    if (feature.unlimited) {
      return this.transloco.translate(`shared.entitlement.${feature.key}.unlimited`);
    }
    if (feature.limit !== null) {
      return this.transloco.translate(`shared.entitlement.${feature.key}.limit`, {count: feature.limit});
    }
    return this.transloco.translate(`shared.entitlement.${feature.key}.granted`);
  }

  private actionLabel(free: boolean, trialDays: number): string {
    if (free) {
      return this.transloco.translate('marketing.pricing.startFree');
    }
    return trialDays > 0
      ? this.transloco.translate('marketing.pricing.startTrial', {days: trialDays})
      : this.transloco.translate('marketing.pricing.choosePlan');
  }
}
