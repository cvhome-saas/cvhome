import {Component, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import type {IdentityProviderDto, IdentityProviderRequest} from '@cvhome-saas/ui-kit/uaa';
import {
  Badge,
  BusyOverlay,
  ConfirmDialog,
  CopyField,
  EmptyState,
  FormDialog,
  FormField,
  LoadError,
  NoticeBar,
  PageHeader,
  Panel,
  SecretField,
  Select,
  TextField,
  Toggle,
} from '@cvhome-saas/ui-kit/ui';
import {IdentityProvidersFacade} from './facades/identity-providers.facade';
import {IDENTITY_PROVIDERS_PROVIDERS} from './services/identity-providers.api.service';

/**
 * How a shopper signs in to this store.
 *
 * Replaces the three-preset screen this console used to have. That one offered Google, Facebook and
 * GitHub, whose endpoints are hard-coded; this one offers every kind the server supports, including
 * a generic OIDC or OAuth2 provider whose endpoints a merchant types.
 *
 * **Which is why the endpoint fields carry a warning rather than a placeholder.** The server fetches
 * every URL here — when it is saved, when Test is pressed, and on every sign-in through the provider
 * — so it refuses any that is not public HTTPS or that resolves inside its own network, and it says
 * only that the endpoint is not allowed. The screen states the rule up front so a refusal reads as a
 * rule rather than a bug.
 *
 * The client secret is never returned. An empty secret field on an existing provider means "keep the
 * stored one", never "clear it", which is what `hasClientSecret` is for.
 */
@Component({
  selector: 'app-identity-providers',
  imports: [
    Badge,
    BusyOverlay,
    ConfirmDialog,
    CopyField,
    EmptyState,
    FormDialog,
    FormField,
    LoadError,
    NoticeBar,
    PageHeader,
    Panel,
    ReactiveFormsModule,
    SecretField,
    Select,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  providers: [...IDENTITY_PROVIDERS_PROVIDERS, IdentityProvidersFacade],
  templateUrl: './identity-providers.html',
  styleUrl: './identity-providers.css',
})
export class IdentityProviders {
  protected readonly facade = inject(IdentityProvidersFacade);

  /** Which preset the open form is for, so the endpoint fields appear only where they are needed. */
  protected readonly chosenPreset = signal('GENERIC_OIDC');

  protected readonly form = new FormGroup({
    alias: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.pattern(/^[a-z0-9-]{2,50}$/)]}),
    displayName: new FormControl('', {nonNullable: true}),
    clientId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    clientSecret: new FormControl('', {nonNullable: true}),
    issuerUri: new FormControl('', {nonNullable: true}),
    authorizationUri: new FormControl('', {nonNullable: true}),
    tokenUri: new FormControl('', {nonNullable: true}),
    userInfoUri: new FormControl('', {nonNullable: true}),
    jwkSetUri: new FormControl('', {nonNullable: true}),
    emailDomains: new FormControl('', {nonNullable: true}),
    trustEmailVerified: new FormControl(true, {nonNullable: true}),
    jitProvisioning: new FormControl(true, {nonNullable: true}),
  });

  protected readonly open = computed(() => this.facade.creating() || !!this.facade.editing());

  protected readonly presetOptions = computed(() =>
    this.facade.presets().map((preset) => ({value: preset.preset, label: preset.displayName})),
  );

  /** A preset with hard-coded endpoints needs none typed; a generic one needs an issuer or the four. */
  protected readonly needsEndpoints = computed(() => {
    const preset = this.facade.presetOf(this.chosenPreset());
    return preset ? preset.needsIssuer || preset.needsEndpoints : true;
  });

  protected startCreate(): void {
    this.form.reset({trustEmailVerified: true, jitProvisioning: true});
    this.chosenPreset.set(this.facade.presets()[0]?.preset ?? 'GENERIC_OIDC');
    this.facade.startCreate();
  }

  protected startEdit(provider: IdentityProviderDto): void {
    this.chosenPreset.set(provider.preset);
    this.form.reset({
      alias: provider.alias,
      displayName: provider.displayName ?? '',
      clientId: provider.clientId,
      clientSecret: '',
      issuerUri: provider.issuerUri ?? '',
      authorizationUri: provider.authorizationUri ?? '',
      tokenUri: provider.tokenUri ?? '',
      userInfoUri: provider.userInfoUri ?? '',
      jwkSetUri: provider.jwkSetUri ?? '',
      emailDomains: provider.emailDomains.join(', '),
      trustEmailVerified: provider.trustEmailVerified,
      jitProvisioning: provider.jitProvisioning,
    });
    this.facade.startEdit(provider);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: IdentityProviderRequest = {
      alias: value.alias.trim(),
      displayName: value.displayName.trim() || value.alias.trim(),
      preset: this.chosenPreset() as IdentityProviderRequest['preset'],
      hideOnLogin: false,
      clientId: value.clientId.trim(),
      // Blank means "keep the stored one": the read never returns a secret, so an empty field on an
      // edit is the absence of a change, not the absence of a secret.
      clientSecret: value.clientSecret.trim() || null,
      issuerUri: value.issuerUri.trim() || null,
      authorizationUri: value.authorizationUri.trim() || null,
      tokenUri: value.tokenUri.trim() || null,
      userInfoUri: value.userInfoUri.trim() || null,
      jwkSetUri: value.jwkSetUri.trim() || null,
      scopes: [],
      userNameAttribute: null,
      clientAuthMethod: null,
      emailDomains: value.emailDomains
        .split(',')
        .map((domain) => domain.trim())
        .filter(Boolean),
      accountLinking: 'LINK',
      jitProvisioning: value.jitProvisioning,
      defaultRoles: [],
      trustEmailVerified: value.trustEmailVerified,
      attributeMapping: {},
    };
    this.facade.save(request);
  }

  protected testResultFor(provider: IdentityProviderDto) {
    const tested = this.facade.tested();
    return tested?.id === provider.id ? tested.result : null;
  }
}
