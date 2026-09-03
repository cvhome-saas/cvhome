import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin} from 'rxjs';

import {ApiErrorService, optionalOne, snapshot} from '@cvhome-saas/ui-kit';
import {
  AdminIdpService,
  type IdentityProviderDto,
  type IdentityProviderRequest,
  type IdpPresetDto,
  type IdpTestResult,
} from '@cvhome-saas/ui-kit/uaa';
import {ToastService} from '@cvhome-saas/ui-kit/ui';

/**
 * This store's identity providers, and what its merchant may do to one.
 *
 * The presets ride along with the list: they fill the type chooser and say which endpoint fields a
 * kind needs. Optional, because a provider list is still usable without them — the chooser falls
 * back to what is already configured.
 */
@Injectable()
export class IdentityProvidersFacade {
  private readonly api = inject(AdminIdpService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);

  /** The provider an editor or a confirmation is acting on, or null. */
  readonly editing = signal<IdentityProviderDto | null>(null);
  readonly creating = signal(false);
  readonly deleting = signal<IdentityProviderDto | null>(null);

  /** What the last test found, kept beside the provider it was run for. */
  readonly tested = signal<{readonly id: string; readonly result: IdpTestResult} | null>(null);

  private readonly page = snapshot(
    () => ({}),
    () =>
      forkJoin({
        providers: this.api.list(),
        presets: this.api.presets().pipe(optionalOne()),
      }),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly reload = () => this.page.reload();

  readonly providers = computed<readonly IdentityProviderDto[]>(() => this.page.value()?.providers ?? []);
  readonly presets = computed<readonly IdpPresetDto[]>(() => this.page.value()?.presets ?? []);
  readonly isEmpty = computed(() => !this.isLoading() && this.providers().length === 0);

  /** The preset being edited, so the form knows which endpoint fields to ask for. */
  readonly presetOf = (name: string) => this.presets().find((preset) => preset.preset === name) ?? null;

  startCreate(): void {
    this.creating.set(true);
    this.editing.set(null);
  }

  startEdit(provider: IdentityProviderDto): void {
    this.editing.set(provider);
    this.creating.set(false);
  }

  save(request: IdentityProviderRequest): void {
    const existing = this.editing();
    const call = existing ? this.api.update(existing.id, request) : this.api.create(request);
    this.busy.set(true);
    call.subscribe({
      next: () => {
        this.busy.set(false);
        this.dismiss();
        this.toast.success(this.transloco.translate('identityProviders.toast.saved', {name: request.alias}));
        this.reload();
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  setEnabled(provider: IdentityProviderDto, enabled: boolean): void {
    this.busy.set(true);
    (enabled ? this.api.enable(provider.id) : this.api.disable(provider.id)).subscribe({
      next: () => {
        this.busy.set(false);
        this.reload();
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  /**
   * Reaches the provider.
   *
   * The result is shown against the row rather than as a toast: a merchant runs this while reading
   * the endpoints they just typed, and an answer that vanishes is an answer they have to re-earn.
   * A refusal — an endpoint the server will not fetch, or the store's hourly budget spent — arrives
   * as a normal API error and says which.
   */
  test(provider: IdentityProviderDto): void {
    this.busy.set(true);
    this.tested.set(null);
    this.api.test(provider.id).subscribe({
      next: (result) => {
        this.busy.set(false);
        this.tested.set({id: provider.id, result});
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  confirmDelete(): void {
    const provider = this.deleting();
    if (!provider) {
      return;
    }
    this.busy.set(true);
    this.api.delete(provider.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.dismiss();
        this.toast.success(this.transloco.translate('identityProviders.toast.deleted', {name: provider.alias}));
        this.reload();
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  dismiss(): void {
    this.editing.set(null);
    this.creating.set(false);
    this.deleting.set(null);
  }

  private fail(failure: unknown): void {
    this.busy.set(false);
    this.toast.danger(this.apiErrors.messageFor(failure));
  }
}
