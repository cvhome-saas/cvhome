import {DestroyRef, Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ApiErrorService} from '@core/errors/api-error.service';
import {clearServerErrorsOnChange} from '@core/errors/form-error.utils';
import {
  POD_HEALTH_TONE,
  POD_LIFECYCLE_TONE,
  POD_VISIBILITY_TONE,
  PROVISIONING_STATE_TONE,
  STORE_STATUS_TONE,
  SUBSCRIPTION_STATUS_TONE,
  type PlatformStoreRow,
  type PodDetail,
} from '@models/platform';
import type {EndpointType} from '@models/pod';
import type {Tone} from '@models/ui';
import {PlatformLabel} from '@shared/i18n/platform-label';
import {snapshot} from '@shared/state/snapshot';
import type {SelectOption} from '@shared/ui/select/select';
import {ToastService} from '@shared/ui/toast/toast';
import {PodDetailApi, type OrgChoice} from '../services/pod-detail.api.service';
import {PodFormService, type PodForm} from '../services/pod-form.service';

/** How many of a pod's stores one page shows. */
export const STORES_PAGE_SIZE = 20;

/** Which confirmation is open, if any. */
export type PodPrompt = 'drain' | 'resume' | 'delete' | null;

/**
 * The toast each lever raises, written out rather than built from the prompt name.
 *
 * `` `…toast.${prompt}ed` `` reads naturally and produces `resumeed`. Transloco throws on a missing
 * key, so that is a page that goes down on a successful write — the worst moment to fail.
 */
const LIFECYCLE_TOAST: Readonly<Record<'drain' | 'resume', string>> = {
  drain: 'platform.pod.toast.drained',
  resume: 'platform.pod.toast.resumed',
};

/**
 * One pod: what state it is in, where it routes, and the three levers over it.
 *
 * Also the create form, because they are the same form with one extra control. Splitting them into
 * two features would put two copies of the endpoint rule and the name pattern in the codebase, and
 * the create is the *only* place the owner is settable — so the difference is one field, not one
 * page.
 *
 * **Two of the four lifecycle states have no endpoint.** `PodLifecycleService` exposes `drain` and
 * `resume`; `PROVISIONING → ACTIVE` and `DECOMMISSIONED` do not exist, so a newly registered pod
 * cannot be marked ready — `newEntity` writes it straight to `ACTIVE` — and a retired one cannot be
 * marked retired. See lessons.md, "Pods — two lifecycle states are unreachable".
 */
@Injectable()
export class PodDetailFacade {
  private readonly api = inject(PodDetailApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly labels = inject(PlatformLabel);
  private readonly forms = inject(PodFormService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  /** The pod being read, or null on the create route. Set by the page from the route. */
  readonly podId = signal<string | null>(null);
  readonly isNew = computed(() => this.podId() === null);

  readonly busy = signal(false);
  readonly prompt = signal<PodPrompt>(null);
  /** Whether the edit form is open. Always open on the create route. */
  readonly editing = signal(false);

  private readonly loaded = snapshot(
    // `null` is a real parameter here — the create route — so the gate is a wrapper object rather
    // than the id itself, which would read as "not ready" and leave the resource idle forever.
    () => ({id: this.podId()}),
    (query) => this.api.load(query.id),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly isEmpty = this.loaded.isEmpty;
  readonly reload = () => this.loaded.reload();

  readonly pod = computed<PodDetail | null>(() => this.loaded.value()?.pod ?? null);
  readonly orgs = computed<readonly OrgChoice[]>(() => this.loaded.value()?.orgs ?? []);

  /* ------------------------------------------------------------- the stores on it ---- */

  /** The store search term, as sent to tenancy. Matches a store's name or its id. */
  readonly storeSearch = signal('');

  /** Back to the first page whenever the pod or the term changes. */
  readonly storesPage = linkedSignal<unknown, number>({
    source: () => [this.podId(), this.storeSearch()] as const,
    computation: () => 0,
  });

  /**
   * The stores placed here, on their own key.
   *
   * Gated on the id, so the create route fetches nothing — there is no pod yet to have stores. Its failure is the
   * panel's rather than the page's: a pod's routing, health and lifecycle are still worth reading when tenancy is
   * unreachable, which is exactly when an operator is most likely to be looking at a pod.
   */
  private readonly stores = snapshot(
    () => {
      const id = this.podId();
      return id ? {id, page: this.storesPage(), term: this.storeSearch()} : undefined;
    },
    (query) => this.api.loadStores(query.id, query.page, STORES_PAGE_SIZE, query.term),
  );

  readonly storeRows = computed<readonly PlatformStoreRow[]>(() => this.stores.value()?.rows ?? []);
  readonly storesLoading = this.stores.isLoading;
  readonly storesError = this.stores.error;
  readonly storesTotal = computed(() => this.stores.value()?.totalElements ?? 0);
  readonly storesTotalPages = computed(() => this.stores.value()?.totalPages ?? 0);
  readonly reloadStores = () => this.stores.reload();

  /** Whether the rows on screen answer the term in the box. See the customers page for the failure. */
  readonly storeRowsMatchSearch = computed(() => this.stores.value()?.term === this.storeSearch());

  setStoreSearch(term: string): void {
    this.storeSearch.set(term);
  }

  /**
   * How many stores tenancy has on this pod, or null before it has answered.
   *
   * **Only while nothing is being searched.** `totalElements` is the count for the *query*, so a
   * search narrowing the panel to one row would otherwise make the capacity comparison below claim
   * the registry had drifted. A filtered panel simply stops asserting a total.
   */
  readonly placedStores = computed<number | null>(() => {
    const loaded = this.stores.value();
    return loaded && !loaded.term ? loaded.totalElements : null;
  });

  /** That count as the panel's header carries it, in the reader's numerals. */
  readonly placedStoresLabel = computed(() => {
    this.transloco.activeLang();
    const placed = this.placedStores();
    if (placed === null) {
      return '';
    }
    return this.transloco.translate('platform.pod.stores.count', {
      stores: this.localeFormat.localizeNumber(placed, 'decimal'),
      count: placed,
    });
  });

  /**
   * Whether the registry's own counter disagrees with tenancy.
   *
   * `capacity_stores` is a mirror maintained from tenancy's outbox, so anything that landed on a pod by another
   * route is uncounted — and it is that number, not tenancy's, that `PodPlacementService` decides placements
   * against. Tenancy reconciles the registry at startup, so after the first boot this should be false; when it is
   * not, something has drifted since and the operator should know which figure governs.
   */
  readonly capacityDisagrees = computed(() => {
    const placed = this.placedStores();
    const pod = this.pod();
    return pod !== null && placed !== null && placed !== pod.capacityStores;
  });

  storeStatusLabel(status: string | null): string {
    return this.labels.storeStatus(status);
  }

  storeStatusTone(status: string | null): Tone {
    return (status && STORE_STATUS_TONE[status as keyof typeof STORE_STATUS_TONE]) || 'slate';
  }

  provisioningLabel(state: string | null): string {
    return this.labels.provisioningState(state);
  }

  provisioningTone(state: string | null): Tone {
    return (state && PROVISIONING_STATE_TONE[state as keyof typeof PROVISIONING_STATE_TONE]) || 'slate';
  }

  billingLabel(status: string | null): string {
    return this.labels.subscriptionStatus(status);
  }

  billingTone(status: string | null): Tone {
    return (status && SUBSCRIPTION_STATUS_TONE[status as keyof typeof SUBSCRIPTION_STATUS_TONE]) || 'slate';
  }

  /** An organization named where the page's own lookup reached it, and its id where it did not. */
  orgLabel(orgId: string): string {
    if (!orgId) {
      return '';
    }
    return this.orgs().find((org) => org.id === orgId)?.label ?? orgId;
  }

  /**
   * The form, held as state rather than derived.
   *
   * A `computed` over the loaded pod would rebuild it whenever the resource re-read — the value is a
   * new object every time — so a half-typed name would be silently replaced underneath the operator,
   * and `clearServerErrorsOnChange` would resubscribe on every run.
   */
  private readonly formState = signal<PodForm | null>(null);
  readonly form = this.formState.asReadonly();

  /* ---------------------------------------------------------------------- rendering ---- */

  readonly heading = computed(() => {
    this.transloco.activeLang();
    const pod = this.pod();
    /*
     * Resolved before the `||`. TypeScript contextually types the right operand of `||` from the
     * left one, so `pod?.name || translate(…)` infers Transloco's `T` as `string | undefined` and
     * the whole heading becomes optional — which fails only in the AOT template check.
     */
    const fallback: string = this.transloco.translate('platform.pod.heading.fallback');
    return {
      title: this.isNew() ? this.transloco.translate('platform.pod.heading.new') : pod?.name || fallback,
      context: this.isNew()
        ? this.transloco.translate('platform.pod.heading.newContext')
        : this.transloco.translate('platform.pod.heading.context', {id: pod?.shortId ?? '—'}),
    };
  });

  readonly lifecycleLabel = computed(() => this.labels.podLifecycle(this.pod()?.lifecycleState ?? null));
  readonly lifecycleTone = computed<Tone>(() => {
    const state = this.pod()?.lifecycleState;
    return (state && POD_LIFECYCLE_TONE[state]) || 'slate';
  });

  readonly healthLabel = computed(() => this.labels.podHealth(this.pod()?.lastHealthStatus ?? null));
  readonly healthTone = computed<Tone>(() => {
    const status = this.pod()?.lastHealthStatus;
    return (status && POD_HEALTH_TONE[status]) || 'slate';
  });

  readonly visibilityLabel = computed(() => this.labels.podVisibility(this.pod()?.visibility ?? null));
  readonly visibilityTone = computed<Tone>(() => {
    const visibility = this.pod()?.visibility;
    return (visibility && POD_VISIBILITY_TONE[visibility]) || 'slate';
  });

  /**
   * When the pod was last probed, or the fact that it never has been.
   *
   * `PodHealthProbe` sweeps every minute, so in practice this is a recent timestamp — "never" is what
   * a pod registered since the last sweep shows, and what every pod shows on a registry that has just
   * started. The *history* behind it is written and unreadable: `pod_health_check` has a row per
   * probe and no endpoint returns any of them. See lessons.md, "Pods — health history and audit are
   * written and never read".
   */
  readonly lastHealthAt = computed(() => {
    this.transloco.activeLang();
    const at = this.pod()?.lastHealthAt;
    return at
      ? this.localeFormat.localizeDate(at, undefined, {dateStyle: 'medium', timeStyle: 'short'})
      : this.transloco.translate('platform.pod.neverProbed');
  });

  /** Capacity as a fraction of the ceiling, or null where there is no ceiling. */
  readonly capacityPercent = computed<number | null>(() => {
    const pod = this.pod();
    if (!pod?.capacityMaxStores) {
      return null;
    }
    return Math.min(100, Math.round((pod.capacityStores / pod.capacityMaxStores) * 100));
  });

  /** "12 of 50", or "12 stores, no ceiling" — a null maximum means unlimited, which is every pod. */
  readonly capacityLabel = computed(() => {
    this.transloco.activeLang();
    const pod = this.pod();
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    if (!pod) {
      return '—';
    }
    return pod.capacityMaxStores === null
      ? this.transloco.translate('platform.pod.capacityUnlimited', {stores: digits(pod.capacityStores)})
      : this.transloco.translate('platform.pod.capacityOf', {
          stores: digits(pod.capacityStores),
          max: digits(pod.capacityMaxStores),
        });
  });

  /** Amber past three quarters, red at the ceiling — the track's own three tones. */
  readonly capacityTone = computed<'primary' | 'amber' | 'red'>(() => {
    const percent = this.capacityPercent();
    if (percent === null) {
      return 'primary';
    }
    return percent >= 100 ? 'red' : percent >= 75 ? 'amber' : 'primary';
  });

  readonly canDrain = computed(() => this.pod()?.lifecycleState === 'ACTIVE');
  readonly canResume = computed(() => this.pod()?.lifecycleState === 'DRAINING');

  readonly endpointTypes = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return (['EXTERNAL', 'INTERNAL'] satisfies EndpointType[]).map((type) => ({
      value: type,
      label: this.transloco.translate(`platform.pod.endpointType.${type}`),
    }));
  });

  /** The owner picker. The empty value is a shared pod, which is what the server reads as PUBLIC. */
  readonly ownerOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.pods.public')},
      ...this.orgs().map((org) => ({value: org.id, label: org.label})),
    ];
  });

  /* ------------------------------------------------------------------------- writes ---- */

  /** Opens the edit form, seeded from the loaded pod. The create route opens it with nothing in it. */
  startEdit(): void {
    const form = this.newForm(this.isNew() ? 'create' : 'edit');
    const pod = this.pod();
    if (pod) {
      this.forms.patchFrom(form, pod);
    }
    this.formState.set(form);
    this.editing.set(true);
  }

  cancelEdit(): void {
    this.editing.set(false);
    this.formState.set(null);
    if (this.isNew()) {
      void this.router.navigate(['/platform/pods']);
    }
  }

  /**
   * Creates or updates, then re-reads.
   *
   * On an edit only the name and the endpoint are sent, because those are the only two
   * `PodServiceImpl.update` reads. On a create the owner rides along and the server derives
   * visibility from it — the one moment either is decided.
   */
  save(): void {
    const form = this.form();
    if (!form || this.busy()) {
      return;
    }
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    const value = form.getRawValue();
    const endpoint = {endpoint: value.endpoint.trim(), type: value.endpointType};

    this.busy.set(true);
    if (this.isNew()) {
      this.api.create({name: value.name.trim(), endpoint, orgId: value.orgId || null}).subscribe({
        next: (id) => {
          this.busy.set(false);
          this.toast.success(this.transloco.translate('platform.pod.toast.created', {name: value.name}));
          // To the detail route, which reads the view: the create answered the routing record only.
          void this.router.navigate(['/platform/pods', id]);
        },
        error: (failure: unknown) => this.fail(failure, form),
      });
      return;
    }

    const id = this.podId();
    if (!id) {
      this.busy.set(false);
      return;
    }
    this.api.update(id, value.name.trim(), endpoint).subscribe({
      next: () => {
        this.busy.set(false);
        this.editing.set(false);
        this.formState.set(null);
        this.toast.success(this.transloco.translate('platform.pod.toast.updated', {name: value.name}));
        this.loaded.reload();
      },
      error: (failure: unknown) => this.fail(failure, form),
    });
  }

  ask(prompt: PodPrompt): void {
    this.prompt.set(prompt);
  }

  dismissPrompt(): void {
    this.prompt.set(null);
  }

  /** Carries out whichever lever is being confirmed. Delete navigates away; the others re-read. */
  confirmPrompt(): void {
    const id = this.podId();
    const prompt = this.prompt();
    if (!id || !prompt || this.busy()) {
      return;
    }
    const name = this.pod()?.name ?? '';
    this.busy.set(true);

    if (prompt === 'delete') {
      this.api.delete(id).subscribe({
        next: () => {
          this.busy.set(false);
          this.prompt.set(null);
          this.toast.success(this.transloco.translate('platform.pod.toast.deleted', {name}));
          void this.router.navigate(['/platform/pods']);
        },
        error: (failure: unknown) => this.fail(failure),
      });
      return;
    }

    const request = prompt === 'drain' ? this.api.drain(id) : this.api.resume(id);
    request.subscribe({
      next: () => {
        this.busy.set(false);
        this.prompt.set(null);
        this.toast.success(this.transloco.translate(LIFECYCLE_TOAST[prompt], {name}));
        this.loaded.reload();
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  /** Always together: a server error is not a validator, so nothing else will ever remove it. */
  private newForm(mode: 'create' | 'edit'): PodForm {
    const form = this.forms.build(mode);
    clearServerErrorsOnChange(form, this.destroyRef);
    return form;
  }

  /**
   * A failed write.
   *
   * A duplicate name arrives as `DuplicatePodNameException` — a 409 that names the field, so
   * `applyToForm` can bind it where there is a form. Everything else is the toast's.
   */
  private fail(failure: unknown, form?: PodForm): void {
    this.busy.set(false);
    if (form) {
      this.apiErrors.applyToForm(failure, form);
    }
    this.toast.danger(this.apiErrors.messageFor(failure));
  }
}
