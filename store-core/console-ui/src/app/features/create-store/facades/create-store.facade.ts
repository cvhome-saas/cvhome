import {DestroyRef, computed, inject, Injectable, signal} from '@angular/core';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl, AsyncValidatorFn, NonNullableFormBuilder, ValidationErrors, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {EMPTY, Observable, Subscription, catchError, first, map, of, switchMap, timer} from 'rxjs';

import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {PodService} from '@api/pod-registry/pod.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {COUNTRIES, DEFAULT_COUNTRY_ID, NEXT_STEPS, PROVISIONING_ARTIFACTS} from '@mocks/create-store.fixture';
import type {CreateStorePhase} from '@models/create-store';
import type {Pod} from '@models/pod';
import type {ManagerStore, ProvisioningState} from '@models/tenancy';

/** How often the new store's row is re-read while it builds. */
const POLL_MS = 2000;

/**
 * How long to keep polling before giving up on an answer.
 *
 * Giving up is not the same as failing: the store row exists either way and provisioning may still
 * finish server-side, so the page says it lost track rather than that anything went wrong.
 */
const POLL_TIMEOUT_MS = 120_000;

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

export interface InfraRow {
  readonly label: string;
  readonly value: string;
  readonly pending: boolean;
}

/**
 * Drives the "Create store" flow.
 *
 * Provisioning is asynchronous on the server: `POST store-manager/private/store` answers as soon as the
 * row exists, with `provisioningState` still in progress, and the only way to watch the rest is to
 * re-read the row. There is no per-step progress, no failure reason and no retry — see lessons.md,
 * "Shell — provisioning has four states and no detail". An earlier revision of this file animated a
 * seven-row checklist against a client-side timer, which meant the page reported success at a fixed
 * moment regardless of what the server was doing, and could never show a failure at all.
 */
@Injectable({providedIn: 'root'})
export class CreateStoreFacade {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly console = inject(ConsoleApi);
  private readonly stores = inject(ManagerStoreService);
  private readonly pods = inject(PodService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly countries = COUNTRIES;
  readonly nextSteps = NEXT_STEPS;

  /**
   * The pods this operator may ask for.
   *
   * Normally empty — `pod/list` returns only an org's own private pods — in which case the form drops
   * the control entirely and the registry places the store. See lessons.md.
   */
  private readonly podList = rxResource({stream: () => this.pods.list()});
  readonly pods$ = computed<readonly Pod[]>(() => this.podList.value() ?? []);
  readonly podChoiceAvailable = computed(() => this.pods$().length > 0);

  readonly form = this.fb.group({
    name: this.fb.control('', {
      validators: [Validators.required, Validators.minLength(2)],
      asyncValidators: [this.uniqueName()],
    }),
    countryId: this.fb.control(DEFAULT_COUNTRY_ID, Validators.required),
    currencyCode: this.fb.control(
      COUNTRIES.find((country) => country.id === DEFAULT_COUNTRY_ID)?.currencyCode ?? 'EUR',
      Validators.required,
    ),
    podId: this.fb.control(''),
  });

  readonly phase = signal<CreateStorePhase>('form');
  readonly submitting = signal(false);

  /** The store as the server last described it. Null until create answers. */
  readonly store = signal<ManagerStore | null>(null);

  /** Set when polling ran out of time rather than reaching a settled state. */
  readonly timedOut = signal(false);

  private poll: Subscription | null = null;

  constructor() {
    this.form.controls.countryId.valueChanges.pipe(takeUntilDestroyed()).subscribe((countryId) => {
      if (this.form.controls.currencyCode.pristine) {
        const country = COUNTRIES.find((candidate) => candidate.id === countryId);
        if (country) {
          this.form.controls.currencyCode.setValue(country.currencyCode, {emitEvent: false});
        }
      }
    });
    this.destroyRef.onDestroy(() => this.poll?.unsubscribe());
  }

  readonly selectedCountry = computed(
    () => this.countries.find((country) => country.id === this.form.controls.countryId.value) ?? this.countries[0],
  );

  readonly provisioningState = computed<ProvisioningState | null>(
    () => this.store()?.provisioningState ?? null,
  );

  readonly isDone = computed(() => this.provisioningState() === 'SUCCESSFULLY_PROVISIONING');
  readonly hasFailed = computed(() => this.provisioningState() === 'FAILED_PROVISIONING');

  /** How many stores the account holds. Live rather than a captured fixture, so first run reads 0. */
  readonly storesUsed = computed(() => this.shell.stores().length);

  /** The pod the registry actually placed the store on, resolved to a name when one is known. */
  readonly assignedPod = computed(() => {
    const podId = this.store()?.podId.id;
    if (!podId) {
      return null;
    }
    const known = this.pods$().find((pod) => pod.id?.id === podId);
    return known ? known.name : podId;
  });

  readonly summary = computed<readonly SummaryRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const country = this.selectedCountry();

    return [
      {
        label: t('createStore.summary.storeName'),
        value: this.form.controls.name.value || t('createStore.summary.pendingName'),
        tone: 'default',
      },
      {
        label: t('createStore.summary.countryCurrency'),
        value: `${t(country.labelKey)} · ${this.form.controls.currencyCode.value}`,
        tone: 'default',
      },
      {
        label: t('createStore.summary.region'),
        value: this.podChoiceAvailable()
          ? (this.pods$().find((pod) => pod.id?.id === this.form.controls.podId.value)?.name ??
             t('createStore.summary.regionAutomatic'))
          : t('createStore.summary.regionAutomatic'),
        tone: 'accent',
      },
    ];
  });

  readonly infra = computed<readonly InfraRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const store = this.store();
    if (!store) {
      return [];
    }

    return [
      {label: t('createStore.infra.storeId'), value: store.id, pending: false},
      {
        label: t('createStore.summary.region'),
        value: this.assignedPod() ?? t('createStore.progress.assigning'),
        pending: this.assignedPod() === null,
      },
      {label: t('createStore.infra.status'), value: this.stateLabel(), pending: !this.isDone()},
    ];
  });

  /**
   * What gets created, as the design's "artifacts" list.
   *
   * Kept because the resources are real — a store does get a database, a node and a locale — but the
   * per-item detail no longer claims values (`fra-07`, `pg-14`) the server never reported.
   */
  readonly artifacts = computed<readonly ArtifactRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    return PROVISIONING_ARTIFACTS.map((artifact) => ({
      id: artifact.id,
      label: t(artifact.labelKey),
      detail: t(artifact.detailKey, artifact.detailParams),
      icon: artifact.icon,
    }));
  });

  readonly headline = computed(() => {
    this.transloco.activeLang();
    if (this.hasFailed()) {
      return this.transloco.translate('createStore.progress.failedTitle');
    }
    return this.isDone()
      ? this.transloco.translate('createStore.progress.doneTitle')
      : this.transloco.translate('createStore.progress.runningTitle');
  });

  readonly stateNote = computed(() => {
    this.transloco.activeLang();
    if (this.hasFailed()) {
      return this.transloco.translate('createStore.progress.failedNote');
    }
    if (this.timedOut()) {
      return this.transloco.translate('createStore.progress.timedOutNote');
    }
    return this.isDone()
      ? this.transloco.translate('createStore.progress.doneNote')
      : this.transloco.translate('createStore.progress.runningNote');
  });

  readonly liveSubtitle = computed(() => {
    this.transloco.activeLang();
    const store = this.store();
    if (!store) {
      return this.transloco.translate('createStore.subtitle.form');
    }
    return this.transloco.translate(
      this.isDone() ? 'createStore.subtitle.live' : 'createStore.subtitle.running',
      {name: store.name},
    );
  });

  /**
   * Creates the store, then watches it build.
   *
   * The country and currency ride along in the request body: tenancy types only `name` and `pod` and
   * forwards everything else to the pod untouched, because the rest of a store belongs to merchant's
   * model rather than tenancy's.
   */
  start(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.timedOut.set(false);

    const {name, countryId, currencyCode, podId} = this.form.getRawValue();

    this.console
      .createStore({
        name,
        country: countryId,
        currency: currencyCode,
        ...(podId ? {pod: {id: podId}} : {}),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (store) => {
          this.submitting.set(false);
          this.store.set(store);
          this.phase.set('running');
          // The rail and the guards ask the same question this page just changed the answer to.
          this.shell.refreshStores();
          this.watch(store);
        },
        error: (error: unknown) => {
          this.submitting.set(false);
          // Quota refusals and duplicate names both arrive as field-less problems; applyToForm binds
          // what it can and toasts the rest, which is the right split for a form this small.
          this.apiErrors.applyToForm(error, this.form);
        },
      });
  }

  /** Back to the form. Only offered when nothing was created. */
  reset(): void {
    this.poll?.unsubscribe();
    this.poll = null;
    this.store.set(null);
    this.timedOut.set(false);
    this.phase.set('form');
  }

  /**
   * Re-reads the store until it settles.
   *
   * `NOT_STARTED` and `IN_PROGRESS` are both "still working"; the other two are terminal.
   *
   * A failed read is swallowed **inside** the `switchMap`, not in the subscriber. An inner error that
   * reaches the outer stream terminates it, which would stop the polling on the first blip and leave
   * the page saying "building" forever. The store exists either way; a transient read failure says
   * nothing about it.
   */
  private watch(store: ManagerStore): void {
    const startedAt = Date.now();
    this.poll?.unsubscribe();

    this.poll = timer(POLL_MS, POLL_MS)
      .pipe(
        switchMap(() => this.stores.storeInfo(store.id).pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (latest) => {
          this.store.set(latest);
          if (settled(latest.provisioningState)) {
            this.stop();
            this.shell.refreshStores();
            return;
          }
          if (Date.now() - startedAt > POLL_TIMEOUT_MS) {
            this.timedOut.set(true);
            this.stop();
          }
        },
      });
  }

  private stop(): void {
    this.poll?.unsubscribe();
    this.poll = null;
  }

  private stateLabel(): string {
    const state = this.provisioningState();
    return state
      ? this.transloco.translate(`createStore.state.${state}`)
      : this.transloco.translate('createStore.progress.pending');
  }

  /**
   * Store names are unique platform-wide, so the server is the only thing that can answer this.
   * Debounced because it fires on every keystroke.
   */
  private uniqueName(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const name = String(control.value ?? '').trim();
      if (name.length < 2) {
        return of(null);
      }
      return timer(400).pipe(
        switchMap(() => this.stores.nameExists(name)),
        map((exists) => (exists ? {nameTaken: true} : null)),
        first(),
      );
    };
  }
}

function settled(state: ProvisioningState): boolean {
  return state === 'SUCCESSFULLY_PROVISIONING' || state === 'FAILED_PROVISIONING';
}
