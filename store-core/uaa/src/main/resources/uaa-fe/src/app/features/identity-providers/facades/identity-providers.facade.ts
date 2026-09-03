import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin} from 'rxjs';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService, type SelectOption} from '@cvhome-saas/ui-kit/ui';
import {
  AdminIdpService,
  AdminUserService,
  type AccountLinking,
  type IdentityProviderDto,
  type IdentityProviderRequest,
  type IdpPreset,
  type IdpPresetDto,
  type IdpTestResult,
} from '@cvhome-saas/ui-kit/uaa';

import {newPairArray, pairRow} from '@shared/ui/pair-list/pair-list';

export const ALIAS_PATTERN = /^[a-z0-9-]{2,50}$/;

const LINKINGS: readonly AccountLinking[] = ['CONFIRM', 'LINK', 'REJECT'];

/**
 * The identity providers: the buttons on the sign-in page and everything behind each one.
 *
 * **A preset is a starting point, not a type.** Choosing one fills the endpoints, scopes and mapping the
 * provider is known to want; the stored provider keeps the resolved values, so the form always shows what
 * uaa will actually use. The generic presets leave the endpoints (or the issuer) to the administrator.
 *
 * **Order is the sign-in page's order.** Move up/down rather than drag: two buttons are what a keyboard
 * user gets from a drag list anyway, and the list is short.
 *
 * **The secret is write-only.** The form shows whether one is stored and takes a new one; a blank field
 * on save keeps what is there.
 */
@Injectable()
export class IdentityProvidersFacade {
  private readonly idps = inject(AdminIdpService);
  private readonly users = inject(AdminUserService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  /** The type chooser, then the form. `editing` holds the provider or `'new'` (with `preset` set). */
  readonly choosingType = signal(false);
  readonly editing = signal<IdentityProviderDto | 'new' | null>(null);
  readonly preset = signal<IdpPresetDto | null>(null);
  readonly deleting = signal<IdentityProviderDto | null>(null);
  readonly testResult = signal<IdpTestResult | null>(null);
  readonly section = signal<'connection' | 'mapping' | 'behaviour'>('connection');

  readonly form = new FormGroup({
    alias: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.pattern(ALIAS_PATTERN)]}),
    displayName: new FormControl('', {nonNullable: true}),
    clientId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    clientSecret: new FormControl('', {nonNullable: true}),
    issuerUri: new FormControl('', {nonNullable: true}),
    authorizationUri: new FormControl('', {nonNullable: true}),
    tokenUri: new FormControl('', {nonNullable: true}),
    userInfoUri: new FormControl('', {nonNullable: true}),
    jwkSetUri: new FormControl('', {nonNullable: true}),
    scopes: new FormControl('', {nonNullable: true}),
    userNameAttribute: new FormControl('', {nonNullable: true}),
    clientAuthMethod: new FormControl('', {nonNullable: true}),
    emailDomains: new FormControl('', {nonNullable: true}),
    accountLinking: new FormControl<AccountLinking>('CONFIRM', {nonNullable: true}),
    jitProvisioning: new FormControl(false, {nonNullable: true}),
    trustEmailVerified: new FormControl(true, {nonNullable: true}),
    hideOnLogin: new FormControl(false, {nonNullable: true}),
    attributeMapping: newPairArray(),
  });

  readonly draftRoles = signal<readonly string[]>([]);

  private readonly loaded = snapshot(
    () => ({}),
    () => forkJoin({list: this.idps.list(), presets: this.idps.presets(), roles: this.users.assignableRoles()}),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly reload = () => this.loaded.reload();
  readonly rows = computed(() => this.loaded.value()?.list ?? []);
  readonly presets = computed(() => this.loaded.value()?.presets ?? []);
  readonly roleOptions = computed(() => this.loaded.value()?.roles ?? []);
  /** What the sign-in page will draw, in order. */
  readonly preview = computed(() => this.rows().filter((p) => p.enabled && !p.hideOnLogin));

  readonly linkingOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return LINKINGS.map((value) => ({value, label: this.transloco.translate(`idps.linking.${value}`)}));
  });

  readonly authMethodOptions: readonly SelectOption[] = ['client_secret_basic', 'client_secret_post'].map((value) => ({
    value,
    label: value,
  }));

  // ---------------------------------------------------------------- open / close

  startAdd(): void {
    this.choosingType.set(true);
  }

  choose(preset: IdpPresetDto): void {
    this.choosingType.set(false);
    this.preset.set(preset);
    this.form.reset({
      alias: preset.preset.toLowerCase().replace('generic_', ''),
      displayName: preset.displayName,
      clientId: '',
      clientSecret: '',
      issuerUri: '',
      authorizationUri: '',
      tokenUri: '',
      userInfoUri: '',
      jwkSetUri: '',
      scopes: preset.defaultScopes.join(' '),
      userNameAttribute: '',
      clientAuthMethod: '',
      emailDomains: '',
      accountLinking: 'CONFIRM',
      jitProvisioning: false,
      trustEmailVerified: true,
      hideOnLogin: false,
    });
    this.form.controls.alias.enable();
    setPairs(this.form.controls.attributeMapping, preset.defaultMapping);
    this.draftRoles.set([]);
    this.section.set('connection');
    this.testResult.set(null);
    this.editing.set('new');
  }

  select(provider: IdentityProviderDto): void {
    this.preset.set(this.presets().find((p) => p.preset === provider.preset) ?? null);
    this.form.reset({
      alias: provider.alias,
      displayName: provider.displayName,
      clientId: provider.clientId,
      clientSecret: '',
      issuerUri: provider.issuerUri ?? '',
      authorizationUri: provider.authorizationUri ?? '',
      tokenUri: provider.tokenUri ?? '',
      userInfoUri: provider.userInfoUri ?? '',
      jwkSetUri: provider.jwkSetUri ?? '',
      scopes: provider.scopes.join(' '),
      userNameAttribute: provider.userNameAttribute ?? '',
      clientAuthMethod: provider.clientAuthMethod,
      emailDomains: provider.emailDomains.join(', '),
      accountLinking: provider.accountLinking,
      jitProvisioning: provider.jitProvisioning,
      trustEmailVerified: provider.trustEmailVerified,
      hideOnLogin: provider.hideOnLogin,
    });
    setPairs(this.form.controls.attributeMapping, provider.attributeMapping);
    this.draftRoles.set([...provider.defaultRoles]);
    this.section.set('connection');
    this.testResult.set(null);
    this.editing.set(provider);
  }

  dismiss(): void {
    this.choosingType.set(false);
    this.editing.set(null);
    this.deleting.set(null);
  }

  askDelete(provider: IdentityProviderDto): void {
    this.editing.set(null);
    this.deleting.set(provider);
  }

  toggleRole(role: string): void {
    this.draftRoles.update((held) => (held.includes(role) ? held.filter((r) => r !== role) : [...held, role]));
  }

  addMapping(): void {
    this.form.controls.attributeMapping.push(pairRow());
  }

  removeMapping(index: number): void {
    this.form.controls.attributeMapping.removeAt(index);
  }

  /** The callback to register at the provider, for a provider that exists or one being drafted. */
  redirectUri(): string {
    const target = this.editing();
    if (target && target !== 'new') {
      return target.redirectUri;
    }
    const alias = this.form.controls.alias.value || '<alias>';
    return `${window.location.origin}/login/oauth2/code/${alias}`;
  }

  // ---------------------------------------------------------------- writes

  save(): void {
    const target = this.editing();
    const preset = this.preset();
    if (!target || !preset || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const body = this.toRequest(preset.preset);
    this.busy.set(true);
    const call = target === 'new' ? this.idps.create(body) : this.idps.update(target.id, body);
    call.subscribe({
      next: (saved) => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate(target === 'new' ? 'idps.toast.created' : 'idps.toast.saved', {name: saved.displayName}));
        this.editing.set(null);
        this.loaded.reload();
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  toggleEnabled(provider: IdentityProviderDto): void {
    this.busy.set(true);
    const call = provider.enabled ? this.idps.disable(provider.id) : this.idps.enable(provider.id);
    call.subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate(provider.enabled ? 'idps.toast.disabled' : 'idps.toast.enabled', {name: provider.displayName}));
        this.loaded.reload();
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** Swaps the provider with its neighbour and sends the whole order. */
  move(provider: IdentityProviderDto, delta: -1 | 1): void {
    const aliases = this.rows().map((p) => p.alias);
    const index = aliases.indexOf(provider.alias);
    const target = index + delta;
    if (index < 0 || target < 0 || target >= aliases.length) {
      return;
    }
    [aliases[index], aliases[target]] = [aliases[target], aliases[index]];
    this.busy.set(true);
    this.idps.reorder(aliases).subscribe({
      next: () => {
        this.busy.set(false);
        this.loaded.reload();
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  test(): void {
    const target = this.editing();
    if (!target || target === 'new') {
      return;
    }
    this.busy.set(true);
    this.idps.test(target.id).subscribe({
      next: (result) => {
        this.busy.set(false);
        this.testResult.set(result);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.testResult.set({ok: false, checked: '', discoveredIssuer: null, detail: this.apiErrors.messageFor(failure)});
      },
    });
  }

  confirmDelete(): void {
    const target = this.deleting();
    if (!target) {
      return;
    }
    this.busy.set(true);
    this.idps.delete(target.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.deleting.set(null);
        this.toast.success(this.transloco.translate('idps.toast.deleted', {name: target.displayName}));
        this.loaded.reload();
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  // ---------------------------------------------------------------- mapping

  private toRequest(preset: IdpPreset): IdentityProviderRequest {
    const raw = this.form.getRawValue();
    const mapping: Record<string, string> = {};
    for (const pair of raw.attributeMapping) {
      if (pair.key.trim()) {
        mapping[pair.key.trim()] = pair.value.trim();
      }
    }
    return {
      alias: raw.alias.trim().toLowerCase(),
      displayName: blankToNull(raw.displayName),
      preset,
      hideOnLogin: raw.hideOnLogin,
      clientId: raw.clientId.trim(),
      clientSecret: blankToNull(raw.clientSecret),
      issuerUri: blankToNull(raw.issuerUri),
      authorizationUri: blankToNull(raw.authorizationUri),
      tokenUri: blankToNull(raw.tokenUri),
      userInfoUri: blankToNull(raw.userInfoUri),
      jwkSetUri: blankToNull(raw.jwkSetUri),
      scopes: raw.scopes.split(/[\s,]+/).map((s) => s.trim()).filter(Boolean),
      userNameAttribute: blankToNull(raw.userNameAttribute),
      clientAuthMethod: blankToNull(raw.clientAuthMethod),
      emailDomains: raw.emailDomains.split(/[\s,]+/).map((s) => s.trim()).filter(Boolean),
      accountLinking: raw.accountLinking,
      jitProvisioning: raw.jitProvisioning,
      defaultRoles: this.draftRoles(),
      trustEmailVerified: raw.trustEmailVerified,
      attributeMapping: mapping,
    };
  }

  private failed(failure: unknown): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, this.form);
    this.apiErrors.notify(failure);
  }
}

function setPairs(array: ReturnType<typeof newPairArray>, map: Readonly<Record<string, string>>): void {
  array.clear({emitEvent: false});
  for (const [key, value] of Object.entries(map)) {
    array.push(pairRow(key, value), {emitEvent: false});
  }
}

const blankToNull = (value: string): string | null => (value.trim() ? value.trim() : null);
