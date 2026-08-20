import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';

import {docsUrl} from '@core/config/docs';
import {BrowserStorage} from '@core/platform/browser-storage';
import type {FirstRunSnapshot} from '@models/first-run';
import type {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '@shared/ui/tone';
import {FirstRunApi} from '../services/first-run.api.service';

const TRIAL_KEY = 'cvhome.console.trialStarted';

/** A checklist row, resolved for rendering. */
export interface SetupStepView {
  readonly id: string;
  readonly label: string;
  readonly meta: string;
  readonly position: number;
  readonly active: boolean;
  readonly locked: boolean;
}

export interface GuideView {
  readonly id: string;
  readonly title: string;
  readonly meta: string;
  /** Absolute URL on the documentation site. These rows are links out, not unbuilt buttons. */
  readonly href: string;
}

export interface NextUpView {
  readonly id: string;
  readonly title: string;
  readonly copy: string;
  readonly icon: IconName;
}

export interface LimitView {
  readonly id: string;
  readonly label: string;
  readonly used: string;
  readonly cap: string;
  readonly pct: number;
  readonly note: string;
}

/**
 * The getting-started page's data and its one piece of real state: whether the trial has
 * started.
 *
 * Store provisioning is a trial entitlement, so until the operator starts one there is
 * nothing for "Create store" to do — the button stays disabled and the page says why.
 * That gate is the only decision this page makes; everything else is content.
 */
@Injectable({providedIn: 'root'})
export class FirstRunFacade {
  private readonly api = inject(FirstRunApi);
  private readonly transloco = inject(TranslocoService);
  private readonly storage = inject(BrowserStorage);

  private readonly snapshot = rxResource({stream: () => this.api.loadSnapshot()});

  /**
   * Holds the last good payload so the page stays readable under the veil, and so a failed
   * reload does not blank a page whose whole job is to tell the operator what to do next.
   */
  private readonly loaded = linkedSignal<FirstRunSnapshot | undefined, FirstRunSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);

  /**
   * Persisted rather than held in memory: creating a store is a separate page, so the
   * operator leaves and comes back, and a trial that silently un-started across that
   * navigation would read as a bug.
   */
  private readonly trialStarted = signal(this.storage.getItem(TRIAL_KEY) === 'true');

  readonly trialPending = computed(() => !this.trialStarted());
  readonly trialDays = computed(() => this.loaded()?.trialDays ?? 0);

  startTrial(): void {
    this.storage.setItem(TRIAL_KEY, 'true');
    this.trialStarted.set(true);
  }

  /** Store provisioning is what the trial unlocks, so it is the gate's one consequence. */
  readonly createDisabled = this.trialPending;

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('firstRun.heading.title'),
      context: this.transloco.translate('firstRun.heading.context'),
    };
  });

  /** Amber while the trial is the blocker, green once it is running. */
  readonly notice = computed<{icon: IconName; tone: Tone; message: string}>(() => {
    this.transloco.activeLang();
    const pending = this.trialPending();
    return {
      icon: pending ? 'clock' : 'sparkles',
      tone: pending ? 'amber' : 'green',
      message: pending
        ? this.transloco.translate('firstRun.notice.pending')
        : this.transloco.translate('firstRun.notice.active', {days: this.trialDays()}),
    };
  });

  readonly hero = computed(() => {
    this.transloco.activeLang();
    const pending = this.trialPending();
    return {
      title: this.transloco.translate(pending ? 'firstRun.hero.pending.title' : 'firstRun.hero.ready.title'),
      copy: this.transloco.translate(pending ? 'firstRun.hero.pending.copy' : 'firstRun.hero.ready.copy'),
      meta: this.transloco.translate(pending ? 'firstRun.hero.pending.meta' : 'firstRun.hero.ready.meta'),
    };
  });

  /**
   * The first step is the live one and the rest are locked, because the console genuinely
   * enforces that order — products, pages and gateways all belong to a store.
   */
  readonly steps = computed<readonly SetupStepView[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.steps ?? []).map((step, index) => ({
      id: step.id,
      label: this.transloco.translate(step.labelKey),
      meta: this.transloco.translate(step.metaKey),
      position: index + 1,
      active: index === 0,
      locked: index > 0,
    }));
  });

  /** Nothing is done until a store exists, so the bar shows a sliver, not zero. */
  readonly progressPct = computed(() => (this.steps().length ? 3 : 0));

  readonly stepBadge = computed(() => {
    this.transloco.activeLang();
    const total = this.steps().length;
    return total ? this.transloco.translate('firstRun.hero.stepOf', {step: 1, total}) : '';
  });

  readonly feature = computed(() => {
    this.transloco.activeLang();
    const feature = this.loaded()?.feature;
    if (!feature) {
      return null;
    }
    return {
      title: this.transloco.translate(feature.titleKey),
      copy: this.transloco.translate(feature.copyKey),
      duration: this.transloco.translate(feature.durationKey),
      src: feature.src,
      poster: feature.poster,
    };
  });

  readonly guides = computed<readonly GuideView[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.guides ?? []).map((guide) => ({
      id: guide.id,
      title: this.transloco.translate(guide.titleKey),
      meta: this.transloco.translate('firstRun.guide.meta', {
        duration: this.transloco.translate(guide.durationKey),
        section: this.transloco.translate(guide.sectionKey),
      }),
      href: docsUrl(guide.docPath),
    }));
  });

  readonly nextUp = computed<readonly NextUpView[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.nextUp ?? []).map((card) => ({
      id: card.id,
      title: this.transloco.translate(card.titleKey),
      copy: this.transloco.translate(card.copyKey),
      icon: card.icon,
    }));
  });

  readonly limits = computed<readonly LimitView[]>(() => {
    this.transloco.activeLang();
    return (this.loaded()?.limits ?? []).map((limit) => ({
      id: limit.id,
      label: this.transloco.translate(limit.labelKey),
      used: limit.used,
      cap: limit.cap,
      pct: limit.pct,
      note: this.transloco.translate(limit.noteKey, limit.noteParams),
    }));
  });

  retry(): void {
    this.snapshot.reload();
  }
}
