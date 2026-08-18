import {Injectable, Signal, WritableSignal, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {ContactRequest, ContactTopicId} from '@models/marketing';
import {
  CONTACT_MESSAGE_HINT_KEYS,
  CONTACT_TOPICS,
  MARKETING_CHANNELS,
  MARKETING_METRICS,
  MARKETING_PILLARS,
  MARKETING_PLANS,
  MARKETING_REVIEWS,
  MARKETING_REVIEW_STATS,
  MARKETING_STORES,
} from '@mocks/marketing.fixture';
import {MarketingApi} from '../services/marketing.api.service';

/** The five slots of a review card's rating row, so the template can draw filled and empty stars. */
export const RATING_SLOTS: readonly number[] = [1, 2, 3, 4, 5];

@Injectable({providedIn: 'root'})
export class MarketingFacade {
  private readonly api = inject(MarketingApi);
  private readonly transloco = inject(TranslocoService);

  readonly yearlyBilling = signal(false);
  readonly menuOpen = signal(false);
  readonly selectedTopic: WritableSignal<ContactTopicId> = signal(CONTACT_TOPICS[0].id);
  readonly messageSent = signal(false);

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

  readonly plans = computed(() => {
    this.transloco.activeLang();
    return MARKETING_PLANS.map((plan) => ({
      // Kept alongside the translated name so the template can single out a plan (the
      // "talk to us" tier routes to #contact) without comparing display text.
      nameKey: plan.nameKey,
      name: this.transloco.translate(plan.nameKey),
      monthlyPrice: plan.monthlyPrice,
      description: this.transloco.translate(plan.descriptionKey),
      action: this.transloco.translate(plan.actionKey),
      featured: plan.featured,
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

  price(plan: {monthlyPrice: number}): number {
    return this.yearlyBilling() ? Math.round(plan.monthlyPrice * 0.8) : plan.monthlyPrice;
  }

  sendMessage(request: ContactRequest): void {
    this.api.sendContactMessage(request).subscribe(() => this.messageSent.set(true));
  }
}
