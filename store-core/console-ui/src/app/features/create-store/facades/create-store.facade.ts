import {DestroyRef, computed, inject, Injectable, signal} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {Subscription, interval} from 'rxjs';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {CONSOLE_USER} from '@mocks/console.fixture';
import {
  COUNTRIES,
  DEFAULT_COUNTRY_ID,
  DEFAULT_PLAN_ID,
  DEFAULT_REGION_ID,
  HOSTING_REGIONS,
  NEXT_STEPS,
  PROVISIONING_ARTIFACTS,
  PROVISIONING_TASKS,
  STORE_PLANS,
} from '@mocks/create-store.fixture';
import {SLUG_PATTERN} from '@models/store-settings';
import type {CreateStorePhase, HostingRegionOption, StorePlanOption} from '@models/create-store';

/** Stores this account already has, on the Free plan — the fixture the sidebar rail uses too. */
const FREE_PLAN_STORE_LIMIT = 3;

/** Fake per-task completion timestamps, in seconds — cosmetic, not derived from real timing. */
const TASK_STAMPS_SECONDS: readonly number[] = [2, 9, 21, 34, 44, 52, 64];

const TICK_MS = 420;
/** Ticks per completed task — chosen so the demo run finishes in a handful of seconds. */
const TICKS_PER_TASK = 2.2;

function slugify(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

function formatStamp(totalSeconds: number): string {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

export interface SummaryRow {
  readonly label: string;
  readonly value: string;
  readonly tone: 'default' | 'accent';
}

export interface ArtifactRow {
  readonly id: string;
  readonly label: string;
  readonly detail: string;
  readonly icon: (typeof PROVISIONING_ARTIFACTS)[number]['icon'];
}

export type TaskStatus = 'done' | 'active' | 'queued';

export interface TaskRow {
  readonly id: string;
  readonly label: string;
  readonly detail: string;
  readonly status: TaskStatus;
  readonly stamp: string;
}

export interface InfraRow {
  readonly label: string;
  readonly value: string;
  readonly pending: boolean;
}

/**
 * Drives the "Create store" flow: a form that captures identity, region and plan, and a
 * simulated provisioning run once it is submitted.
 *
 * There is no real backend for store creation yet, so `start()` runs a scripted checklist
 * against a client-side timer rather than polling a job — the same trade the rest of the
 * console makes with fixture-backed facades, just animated instead of static.
 */
@Injectable({providedIn: 'root'})
export class CreateStoreFacade {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly shell = inject(ConsoleShellFacade);

  readonly countries = COUNTRIES;
  readonly regions = HOSTING_REGIONS;
  readonly plans = STORE_PLANS;

  readonly form = this.fb.group({
    name: this.fb.control('', [Validators.required, Validators.minLength(2)]),
    code: this.fb.control('', [Validators.required, Validators.pattern(SLUG_PATTERN)]),
    countryId: this.fb.control(DEFAULT_COUNTRY_ID, Validators.required),
    currencyCode: this.fb.control(
      COUNTRIES.find((country) => country.id === DEFAULT_COUNTRY_ID)!.currencyCode,
      Validators.required,
    ),
  });

  readonly regionId = signal(DEFAULT_REGION_ID);
  readonly planId = signal(DEFAULT_PLAN_ID);

  readonly phase = signal<CreateStorePhase>('form');
  readonly doneCount = signal(0);
  readonly elapsedTicks = signal(0);

  /** Captured when the run starts, so the progress screen does not chase later edits. */
  private readonly runSnapshot = signal<{
    name: string;
    code: string;
    region: HostingRegionOption;
    plan: StorePlanOption;
    currencyCode: string;
  } | null>(null);

  private subscription: Subscription | null = null;

  constructor() {
    this.form.controls.name.valueChanges.subscribe((name) => {
      if (this.form.controls.code.pristine) {
        this.form.controls.code.setValue(slugify(name), {emitEvent: false});
      }
    });
    this.form.controls.countryId.valueChanges.subscribe((countryId) => {
      if (this.form.controls.currencyCode.pristine) {
        const country = COUNTRIES.find((candidate) => candidate.id === countryId);
        if (country) {
          this.form.controls.currencyCode.setValue(country.currencyCode, {emitEvent: false});
        }
      }
    });
    this.destroyRef.onDestroy(() => this.subscription?.unsubscribe());
  }

  readonly selectedCountry = computed(
    () => this.countries.find((country) => country.id === this.form.controls.countryId.value) ?? this.countries[0],
  );
  readonly selectedRegion = computed(
    () => this.regions.find((region) => region.id === this.regionId()) ?? this.regions[0],
  );
  readonly selectedPlan = computed(
    () => this.plans.find((plan) => plan.id === this.planId()) ?? this.plans[0],
  );

  readonly subdomain = computed(() => {
    const code = this.form.controls.code.value;
    return code ? `${code}.myshop.io` : '';
  });

  /** Live rather than a captured fixture length, so first run reads 0 and not 3. */
  readonly storesUsed = computed(() => this.shell.stores().length);
  readonly storeLimit = FREE_PLAN_STORE_LIMIT;
  readonly overLimit = computed(() => this.storesUsed() >= this.storeLimit);

  selectRegion(region: HostingRegionOption): void {
    if (!region.locked) {
      this.regionId.set(region.id);
    }
  }

  selectPlan(plan: StorePlanOption): void {
    this.planId.set(plan.id);
  }

  readonly summary = computed<readonly SummaryRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const country = this.selectedCountry();
    const region = this.selectedRegion();
    const plan = this.selectedPlan();
    const name = this.form.controls.name.value || t('createStore.summary.pendingName');

    return [
      {label: t('createStore.summary.storeName'), value: name, tone: 'default'},
      {label: t('createStore.summary.tenant'), value: this.form.controls.code.value || '—', tone: 'default'},
      {label: t('createStore.summary.region'), value: region.code, tone: 'accent'},
      {
        label: t('createStore.summary.countryCurrency'),
        value: `${t(country.labelKey)} · ${country.currencyCode}`,
        tone: 'default',
      },
      {label: t('createStore.summary.plan'), value: `${t(plan.labelKey)} · ${t('createStore.plan.priceMonthly', {amount: plan.monthlyPrice})}`, tone: 'default'},
      {label: t('createStore.summary.resources'), value: t(plan.specs[0].key, plan.specs[0].params), tone: 'default'},
      {label: t('createStore.summary.estimatedTime'), value: t('createStore.summary.estimatedTimeValue'), tone: 'default'},
    ];
  });

  readonly artifacts = computed<readonly ArtifactRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const region = this.selectedRegion();
    const subdomain = this.subdomain() || t('createStore.summary.pendingSubdomain');

    return PROVISIONING_ARTIFACTS.map((artifact) => {
      const params =
        artifact.id === 'node'
          ? {region: region.code}
          : artifact.id === 'domain'
            ? {domain: subdomain}
            : artifact.id === 'owner'
              ? {name: CONSOLE_USER.name}
              : artifact.detailParams;
      return {
        id: artifact.id,
        label: t(artifact.labelKey),
        detail: t(artifact.detailKey, params),
        icon: artifact.icon,
      };
    });
  });

  readonly tasks = computed<readonly TaskRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const done = this.doneCount();
    const snapshot = this.runSnapshot();

    return PROVISIONING_TASKS.map((task, index) => {
      const status: TaskStatus = index < done ? 'done' : index === done ? 'active' : 'queued';
      const params = (snapshot ? this.taskParamsFor(task.id, snapshot) : undefined) ?? task.detailParams;

      return {
        id: task.id,
        label: t(task.labelKey, params),
        detail: t(task.detailKey, params),
        status,
        stamp:
          status === 'done'
            ? formatStamp(TASK_STAMPS_SECONDS[index])
            : status === 'active'
              ? t('createStore.progress.running')
              : t('createStore.progress.queued'),
      };
    });
  });

  readonly infra = computed<readonly InfraRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const done = this.doneCount();
    const snapshot = this.runSnapshot();
    if (!snapshot) {
      return [];
    }

    return [
      {label: t('createStore.summary.region'), value: snapshot.region.code, pending: false},
      {label: t('createStore.infra.node'), value: done >= 2 ? 'fra-07' : t('createStore.progress.assigning'), pending: done < 2},
      {label: t('createStore.infra.database'), value: done >= 3 ? `${snapshot.code.replace(/-/g, '_')} · pg-14` : t('createStore.progress.pending'), pending: done < 3},
      {label: t('createStore.infra.resources'), value: t(snapshot.plan.specs[0].key, snapshot.plan.specs[0].params), pending: false},
      {label: t('createStore.infra.storage'), value: t(snapshot.plan.specs[1].key, snapshot.plan.specs[1].params), pending: false},
      {label: t('createStore.infra.certificate'), value: done >= 5 ? t('createStore.progress.issued') : t('createStore.progress.pending'), pending: done < 5},
    ];
  });

  readonly isDone = computed(() => this.doneCount() >= PROVISIONING_TASKS.length);
  readonly progressPercent = computed(() => Math.round((this.doneCount() / PROVISIONING_TASKS.length) * 100));

  readonly nextSteps = NEXT_STEPS;

  readonly liveSubtitle = computed(() => {
    this.transloco.activeLang();
    const snapshot = this.runSnapshot();
    if (!snapshot) {
      return this.transloco.translate('createStore.subtitle.form');
    }
    return this.isDone()
      ? this.transloco.translate('createStore.subtitle.live', {name: snapshot.name, region: snapshot.region.code})
      : this.transloco.translate('createStore.subtitle.running', {name: snapshot.name, region: snapshot.region.code});
  });

  readonly headline = computed(() => {
    this.transloco.activeLang();
    return this.isDone()
      ? this.transloco.translate('createStore.progress.doneTitle')
      : this.transloco.translate('createStore.progress.runningTitle');
  });

  readonly headMeta = computed(() => {
    this.transloco.activeLang();
    const done = this.doneCount();
    return this.isDone()
      ? this.transloco.translate('createStore.progress.doneMeta', {count: PROVISIONING_TASKS.length})
      : this.transloco.translate('createStore.progress.stepMeta', {
          step: Math.min(PROVISIONING_TASKS.length, done + 1),
          count: PROVISIONING_TASKS.length,
        });
  });

  readonly etaLabel = computed(() => {
    this.transloco.activeLang();
    if (this.isDone()) {
      return this.transloco.translate('createStore.progress.etaComplete');
    }
    const secondsLeft = Math.max(5, 62 - Math.round(this.elapsedTicks() * 2.4));
    return this.transloco.translate('createStore.progress.etaRemaining', {seconds: secondsLeft});
  });

  readonly liveBanner = computed(() => {
    this.transloco.activeLang();
    const snapshot = this.runSnapshot();
    if (!snapshot) {
      return '';
    }
    return this.transloco.translate('createStore.progress.liveBanner', {
      name: snapshot.name,
      region: snapshot.region.code,
      node: 'fra-07',
      domain: `${snapshot.code}.myshop.io`,
    });
  });

  start(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.subscription?.unsubscribe();
    this.runSnapshot.set({
      name: this.form.controls.name.value,
      code: this.form.controls.code.value,
      region: this.selectedRegion(),
      plan: this.selectedPlan(),
      currencyCode: this.form.controls.currencyCode.value,
    });
    this.doneCount.set(0);
    this.elapsedTicks.set(0);
    this.phase.set('running');

    this.subscription = interval(TICK_MS).subscribe(() => {
      const elapsed = this.elapsedTicks() + 1;
      this.elapsedTicks.set(elapsed);
      const done = Math.min(PROVISIONING_TASKS.length, Math.floor(elapsed / TICKS_PER_TASK));
      this.doneCount.set(done);
      if (done >= PROVISIONING_TASKS.length) {
        this.subscription?.unsubscribe();
        this.subscription = null;
        this.registerProvisionedStore();
      }
    });
  }

  /**
   * Hands the finished store to the console chrome.
   *
   * Until this runs the store exists only as a completed animation: the rail would still
   * show none, and on a first run the guards would bounce the operator straight back to
   * getting-started from every link on this page. Registering it is what makes the
   * provisioning real to the rest of the app.
   */
  private registerProvisionedStore(): void {
    const snapshot = this.runSnapshot();
    if (snapshot) {
      this.shell.registerStore(snapshot.name);
    }
  }

  reset(): void {
    this.subscription?.unsubscribe();
    this.subscription = null;
    this.doneCount.set(0);
    this.elapsedTicks.set(0);
    this.phase.set('form');
  }

  private taskParamsFor(
    taskId: string,
    snapshot: NonNullable<ReturnType<CreateStoreFacade['runSnapshot']>>,
  ): Record<string, string | number> | undefined {
    switch (taskId) {
      case 'validate':
        return {code: snapshot.code};
      case 'allocate':
        return {node: 'fra-07', region: snapshot.region.code};
      case 'database':
        return {schema: snapshot.code.replace(/-/g, '_')};
      case 'seed':
        return {currency: snapshot.currencyCode};
      case 'tls':
        return {domain: `${snapshot.code}.myshop.io`};
      default:
        return undefined;
    }
  }
}
