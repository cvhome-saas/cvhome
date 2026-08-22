import {DestroyRef, computed, inject, Injectable, signal} from '@angular/core';
import {rxResource, takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  AsyncValidatorFn,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {Observable, Subscription, first, map, of, switchMap, timer} from 'rxjs';

import {CreateStoreApi} from '../services/create-store.api.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {defaultLanguageIsSupported} from '@shared/validators/default-language-is-supported';
import {phoneNumber} from '@shared/validators/phone-number';
import {NEXT_STEPS, PROVISIONING_ARTIFACTS} from '../create-store.content';
import type {CreateStorePhase} from '@models/create-store';
import type {Pod} from '@models/pod';
import type {CreateStoreRequest, ManagerStore, ProvisioningState} from '@models/tenancy';

/** How often the new store's row is re-read while it builds. */
const POLL_MS = 2000;

/**
 * How long to keep polling before giving up on an answer.
 *
 * Giving up is not the same as failing: the store row exists either way and provisioning may still
 * finish server-side, so the page says it lost track rather than that anything went wrong.
 */
const POLL_TIMEOUT_MS = 120_000;

/**
 * The units a store measures in. Constants because the server publishes no endpoint for either —
 * they are the `WeightUnit` and `MeasureUnit` enums, and `details-section.ts` reads them the same way.
 */
const WEIGHT_UNITS = ['KG', 'LB'] as const;
const DIMENSION_UNITS = ['CM', 'IN'] as const;

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

/** The choices the create form's selects offer, as the server answers them. */
export interface CreateStoreChoices {
  readonly themes: readonly string[];
  readonly colorThemes: readonly string[];
}

export type CreateStoreForm = FormGroup<{
  name: FormControl<string>;
  email: FormControl<string>;
  phone: FormControl<string>;
  currency: FormControl<string>;
  language: FormControl<string>;
  supportedLanguages: FormControl<readonly string[]>;
  country: FormControl<string>;
  addressLine: FormControl<string>;
  city: FormControl<string>;
  postalCode: FormControl<string>;
  stateProvince: FormControl<string>;
  theme: FormControl<string>;
  colorTheme: FormControl<string>;
  inBusinessSince: FormControl<string>;
  weightUnit: FormControl<string>;
  dimensionUnit: FormControl<string>;
  requireLoginForOrderPlacement: FormControl<boolean>;
  podId: FormControl<string>;
}>;

/**
 * Drives the "Create store" flow.
 *
 * Provisioning is asynchronous on the server: `POST store-manager/private/store` answers as soon as the
 * row exists, with `provisioningState` still in progress, and the only way to watch the rest is to
 * re-read the row. There is no per-step progress and no retry — see lessons.md, "Shell — provisioning
 * has four states and no detail". A failure does now carry a reason, which is `provisioningError`.
 *
 * **The form is the whole merchant store, not a name and a country.** An earlier revision posted four
 * fields — name, country, currency, pod — because tenancy accepted anything and forwarded it. The store
 * row was created, the console reported "building", and the pod refused the create minutes later for a
 * missing email and phone. `merchant.merchant_store` is NOT NULL on the store's email, theme, colour
 * theme, country, currency and default language, `MerchantStoreDetails` is `@NotNull` on email and
 * phone, and `PersistableMerchantStorePopulator` dereferences the language fields unguarded. Every one
 * of those is collected here, and tenancy now validates them at the POST rather than at the pod.
 */
@Injectable({providedIn: 'root'})
export class CreateStoreFacade {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly transloco = inject(TranslocoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly console = inject(ConsoleApi);
  private readonly api = inject(CreateStoreApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly reference = inject(ReferenceDataService);

  readonly nextSteps = NEXT_STEPS;
  readonly weightUnits = WEIGHT_UNITS;
  readonly dimensionUnits = DIMENSION_UNITS;

  /**
   * The pods this operator may ask for.
   *
   * Normally empty — `pod/list` returns only an org's own private pods — in which case the form drops
   * the control entirely and the registry places the store. See lessons.md.
   */
  private readonly podList = rxResource({stream: () => this.api.listPods()});
  readonly pods$ = computed<readonly Pod[]>(() => this.podList.value() ?? []);
  readonly podChoiceAvailable = computed(() => this.pods$().length > 0);

  /**
   * Themes and colour themes, both NOT NULL columns on the store.
   *
   * `optionalList` on each, inside the api service: neither list is worth failing the whole form
   * over, and a store can be created with a theme the operator typed nowhere as long as one is
   * picked. An empty list leaves the select empty and `Validators.required` then says so, which is
   * the honest outcome of a lookup that failed.
   */
  private readonly choiceList = rxResource({stream: () => this.api.loadReference()});
  readonly choices = computed<CreateStoreChoices>(
    () => this.choiceList.value() ?? {themes: [], colorThemes: []},
  );

  /** Countries and currencies come from `Intl`, not the platform — see `ReferenceDataService`. */
  readonly countryOptions = computed<readonly ReferenceOption[]>(() => this.reference.countries());
  readonly currencyOptions = computed<readonly ReferenceOption[]>(() => this.reference.currencies());
  readonly languageOptions = computed<readonly ReferenceOption[]>(() => this.reference.storefrontLanguages());

  /*
   * Required mirrors what the server refuses without, and nothing more.
   *
   * `city` and `postalCode` are in that set even though their columns are nullable: `MerchantStore`
   * the *entity* carries `@NotEmpty` on both, so Hibernate rejects the insert at persist time — and
   * as a 500 rather than a 400, because that fires below the layer that renders field errors. Leaving
   * them optional here is what produced a real FAILED store row carrying `COMMON.INTERNAL_ERROR`
   * during QA. The street line and `stateProvince` genuinely are optional on both.
   */
  readonly form: CreateStoreForm = this.fb.group(
    {
      name: this.fb.control('', {
        validators: [Validators.required, Validators.minLength(2)],
        asyncValidators: [this.uniqueName()],
      }),
      email: this.fb.control('', [Validators.required, Validators.email]),
      phone: this.fb.control('', [Validators.required, phoneNumber]),
      currency: this.fb.control('', [Validators.required]),
      language: this.fb.control('en', [Validators.required]),
      /* `Validators.required` counts `[]` as empty, which is the rule: `supportedLanguages` is `@NotEmpty`. */
      supportedLanguages: this.fb.control<readonly string[]>(['en'], [Validators.required]),
      country: this.fb.control('', [Validators.required]),
      addressLine: this.fb.control(''),
      city: this.fb.control('', [Validators.required]),
      postalCode: this.fb.control('', [Validators.required]),
      stateProvince: this.fb.control(''),
      theme: this.fb.control('', [Validators.required]),
      colorTheme: this.fb.control('', [Validators.required]),
      /* `LocalDate`, so the control holds `YYYY-MM-DD` and binds to `<input type="date">`. */
      inBusinessSince: this.fb.control(''),
      weightUnit: this.fb.control<string>(WEIGHT_UNITS[0]),
      dimensionUnit: this.fb.control<string>(DIMENSION_UNITS[0]),
      requireLoginForOrderPlacement: this.fb.control(true),
      podId: this.fb.control(''),
    },
    {validators: [defaultLanguageIsSupported]},
  );

  /**
   * The form's current value, as a signal.
   *
   * A reactive form is not one, so `computed(() => this.form.getRawValue())` reads the value once
   * and never recomputes — which is exactly what the summary panel did: it sat on "—" while the
   * operator filled the whole form in front of it. Driving it off the form's own event stream is
   * the same idiom store management's details section uses.
   */
  private readonly formEvent = toSignal(this.form.events, {initialValue: null});
  private readonly formValue = computed(() => {
    this.formEvent();
    return this.form.getRawValue();
  });

  readonly phase = signal<CreateStorePhase>('form');
  readonly submitting = signal(false);

  /** The store as the server last described it. Null until create answers. */
  readonly store = signal<ManagerStore | null>(null);

  /** Set when polling ran out of time rather than reaching a settled state. */
  readonly timedOut = signal(false);

  private poll: Subscription | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.poll?.unsubscribe());
  }

  readonly provisioningState = computed<ProvisioningState | null>(
    () => this.store()?.provisioningState ?? null,
  );

  readonly isDone = computed(() => this.provisioningState() === 'SUCCESSFULLY_PROVISIONING');
  readonly hasFailed = computed(() => this.provisioningState() === 'FAILED_PROVISIONING');

  /**
   * The pod's own words for why the build failed, or null.
   *
   * Not translated and not parsed: it is whatever the pod's problem body said, which for a validation
   * failure names the fields. Shown under the translated failure line so the operator has something to
   * act on — before tenancy recorded this, "failed" was the entire answer.
   */
  readonly failureReason = computed(() => this.store()?.provisioningError ?? null);

  /** How many stores the account holds. Live rather than a captured fixture, so first run reads 0. */
  readonly storesUsed = computed(() => this.shell.stores().length);

  /**
   * The pod the registry actually placed the store on, resolved to a name when one is known.
   *
   * Falls back to the bare pod id, which is right for the "Assigned infrastructure" panel — an
   * operator chasing a placement wants the identifier — and wrong for prose. See
   * {@link assignedPodName}.
   */
  readonly assignedPod = computed(() => {
    const podId = this.store()?.podId.id;
    if (!podId) {
      return null;
    }
    const known = this.pods$().find((pod) => pod.id?.id === podId);
    return known ? known.name : podId;
  });

  /**
   * The pod's *name*, or null when only its id is known.
   *
   * Distinct from {@link assignedPod} because a sentence and a technical panel want different
   * answers. `pod/list` returns only an org's own private pods, so for most merchants the placement
   * resolves to nothing but a hex id — and "live in 507f1f77bcf86cd799439011" is not a region, it is
   * a leak of an internal identifier into a headline.
   */
  private readonly assignedPodName = computed(() => {
    const podId = this.store()?.podId.id;
    if (!podId) {
      return null;
    }
    return this.pods$().find((pod) => pod.id?.id === podId)?.name ?? null;
  });

  readonly summary = computed<readonly SummaryRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    const value = this.formValue();

    return [
      {
        label: t('createStore.summary.storeName'),
        value: value.name || t('createStore.summary.pendingName'),
        tone: 'default',
      },
      {
        label: t('createStore.summary.countryCurrency'),
        value: [this.labelFor(this.countryOptions(), value.country), value.currency]
          .filter(Boolean)
          .join(' · ') || t('createStore.summary.pendingName'),
        tone: 'default',
      },
      {
        label: t('createStore.summary.languages'),
        value: value.supportedLanguages
          .map((code) => this.reference.languageName(code))
          .join(', ') || t('createStore.summary.pendingName'),
        tone: 'default',
      },
      {
        label: t('createStore.summary.region'),
        value: this.podChoiceAvailable()
          ? (this.pods$().find((pod) => pod.id?.id === value.podId)?.name ??
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
   * detail lines no longer name a region, a subdomain or an owner. Nothing knows any of those before
   * provisioning: the old copy interpolated params the fixture supplied, and once those went the lines
   * rendered the word "undefined" to the operator.
   */
  readonly artifacts = computed<readonly ArtifactRow[]>(() => {
    this.transloco.activeLang();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);
    return PROVISIONING_ARTIFACTS.map((artifact) => ({
      id: artifact.id,
      label: t(artifact.labelKey),
      detail: t(artifact.detailKey),
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

  /**
   * The page's context line.
   *
   * Two variants of each state, picked on whether the pod resolved to a name. The single variant
   * this replaces interpolated `{region}` and was only ever passed `{name}`, so the finished page
   * read "Acme · live in undefined" — a leftover from when the region came from a fixture that
   * supplied one. A missing value has to change the sentence, not appear inside it.
   */
  readonly liveSubtitle = computed(() => {
    this.transloco.activeLang();
    const store = this.store();
    if (!store) {
      return this.transloco.translate('createStore.subtitle.form');
    }
    const region = this.assignedPodName();
    const state = this.isDone() ? 'live' : 'running';
    return region
      ? this.transloco.translate(`createStore.subtitle.${state}InRegion`, {name: store.name, region})
      : this.transloco.translate(`createStore.subtitle.${state}`, {name: store.name});
  });

  /** Whether the given language is ticked. Bound per checkbox rather than via a control per language. */
  isSupported(code: string): boolean {
    return this.form.controls.supportedLanguages.value.includes(code);
  }

  /**
   * Ticks or unticks one supported language.
   *
   * A new array rather than a mutation: `FormControl.setValue` compares by reference, and mutating in
   * place leaves every `computed` reading this control showing the previous set.
   */
  toggleLanguage(code: string): void {
    const control = this.form.controls.supportedLanguages;
    const current = control.value;
    control.setValue(
      current.includes(code) ? current.filter((entry) => entry !== code) : [...current, code],
    );
    control.markAsDirty();
  }

  /**
   * Writes a switch's new state onto its control.
   *
   * `app-toggle` is a `button role="switch"` rather than an input, so it cannot be driven by
   * `formControlName` — the control is written here, exactly as store management's details section
   * does it. `markAsDirty` because a switch the operator flipped is a change they made.
   */
  setRequireLogin(checked: boolean): void {
    const control = this.form.controls.requireLoginForOrderPlacement;
    control.setValue(checked);
    control.markAsDirty();
  }

  /**
   * Creates the store, then watches it build.
   *
   * The body is a whole merchant store. `address` is sent as an object because that is the shape
   * merchant's `PersistableBaseAddress` deserializes; `country` alone is required inside it.
   */
  start(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.timedOut.set(false);

    this.console
      .createStore(this.payload())
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
          // Quota refusals and duplicate names arrive as field-less problems; a body tenancy validated
          // arrives with field errors that bind. applyToForm makes that split, which is why the create
          // endpoint being `@Valid` matters to this page and not only to the server.
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
   * The form as tenancy's `CreateStoreRequest`.
   *
   * `inBusinessSince` is omitted rather than sent empty — it is a `LocalDate` and `''` does not parse.
   * The optional address parts are sent as typed, blanks included, because merchant stores them
   * nullable and an empty string is what "not given" looks like on the way in.
   */
  private payload(): CreateStoreRequest {
    const value = this.form.getRawValue();
    return {
      name: value.name.trim(),
      email: value.email.trim(),
      phone: value.phone.trim(),
      theme: value.theme,
      colorTheme: value.colorTheme,
      currency: value.currency,
      defaultLanguage: value.language,
      supportedLanguages: value.supportedLanguages,
      address: {
        country: value.country,
        city: value.city.trim(),
        postalCode: value.postalCode.trim(),
        stateProvince: value.stateProvince.trim(),
        address: value.addressLine.trim(),
      },
      weight: value.weightUnit,
      dimension: value.dimensionUnit,
      requireLoginForOrderPlacement: value.requireLoginForOrderPlacement,
      ...(value.inBusinessSince ? {inBusinessSince: value.inBusinessSince} : {}),
      ...(value.podId ? {pod: {id: value.podId}} : {}),
    };
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
        switchMap(() => this.api.storeInfo(store.id)),
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

  private labelFor(options: readonly ReferenceOption[], code: string): string {
    return options.find((option) => option.code === code)?.label ?? code;
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
        switchMap(() => this.api.nameExists(name)),
        map((exists) => (exists ? {nameTaken: true} : null)),
        first(),
      );
    };
  }
}

function settled(state: ProvisioningState): boolean {
  return state === 'SUCCESSFULLY_PROVISIONING' || state === 'FAILED_PROVISIONING';
}
