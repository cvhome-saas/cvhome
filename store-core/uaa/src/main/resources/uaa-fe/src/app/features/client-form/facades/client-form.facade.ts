import {Injectable, computed, effect, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {FormArray, FormControl, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin, of} from 'rxjs';

import {ApiErrorService, optionalOne, snapshot} from '@cvhome-saas/ui-kit';
import {slugify, uriValidator} from '@cvhome-saas/ui-kit/forms';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {newPairArray, pairRow, type PairArray} from '@shared/ui/pair-list/pair-list';
import {
  AdminClientService,
  type ClientDetails,
  type ClientOptions,
  type ClientSettings,
  type ClientTokenSettings,
  type RotatedSecret,
} from '@cvhome-saas/ui-kit/uaa';

/** A secret the console has just been handed and will never see again once the dialog closes. */
export interface ShownSecret {
  readonly clientId: string;
  readonly secret: string;
  readonly expiresAt: string | null;
  readonly previousUntil: string | null;
}

/** What uaa will accept as a client id, and what `Generate` produces. */
export const CLIENT_ID_PATTERN = /^[a-z0-9][a-z0-9-]{2,}$/;

/** The auth method that means "public client": no secret is issued, so PKCE is the only protection. */
const AUTH_NONE = 'none';
const AUTH_PRIVATE_KEY_JWT = 'private_key_jwt';
const AUTH_TLS_CLIENT_AUTH = 'tls_client_auth';
const GRANT_AUTHORIZATION_CODE = 'authorization_code';

const EMPTY_OPTIONS: ClientOptions = {
  clientAuthenticationMethods: [],
  authorizationGrantTypes: [],
  scopes: [],
  idTokenSignatureAlgorithm: [],
  tokenEndpointAuthenticationSigningAlgorithm: [],
  accessTokenFormat: [],
  clientTypes: [],
};

/** One line of the readiness panel: a rule, whether it is met, and why it applies right now. */
export interface ReadinessCheck {
  readonly id: string;
  readonly ok: boolean;
  /** The i18n key for the sentence, which changes with the shape of the client being registered. */
  readonly key: string;
}

/** The repeating URI shape on this form, named so the row component can take it. */
export type UriArray = FormArray<FormControl<string>>;

/**
 * One OAuth client, whole.
 *
 * **Why this is a route and not a pane.** `ClientDetails` carries five groups of settings, two URI
 * arrays and two open key/value maps. The pane this replaced edited seven comma-separated strings
 * and carried the rest through untouched, which meant `requireProofKey`, the JWK Set URL, three
 * signature algorithms and both custom-settings maps had no screen at all — while
 * `GET /clients/options` was already answering the enum lists for them.
 *
 * **Every list here is picked, not typed.** The option lists come from the server's own enums, so a
 * checkbox group cannot offer a grant type uaa would reject; the comma-separated fields it replaced
 * could, and only said so in a hint.
 *
 * **The readiness panel is not decoration.** Each line is a rule the OAuth spec or uaa enforces
 * somewhere less visible — a public client with no PKCE, `private_key_jwt` with no JWK Set URL — and
 * every one of them is also a real validator on this form. The panel is where they become legible
 * before the save, rather than a 400 afterwards.
 */
@Injectable()
export class ClientFormFacade {
  private readonly clients = inject(AdminClientService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly busy = signal(false);
  readonly rotating = signal(false);
  readonly deleting = signal(false);
  readonly revokingPrevious = signal(false);
  /** The secret just issued — by registration or rotation — shown once. */
  readonly shownSecret = signal<ShownSecret | null>(null);

  /**
   * The route's `:id`, or `null` on `/clients/new`.
   *
   * Declared before everything that reads it: `snapshot` evaluates its query during construction,
   * and a field initialised later is `undefined` at that moment rather than merely empty.
   */
  private readonly params = toSignal(this.route.paramMap, {initialValue: null});

  /** The id in the URL, or `null` on `/clients/new`. */
  currentId(): string | null {
    return this.params()?.get('id') ?? null;
  }

  readonly form = new FormGroup(
    {
      clientId: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.pattern(CLIENT_ID_PATTERN)],
      }),
      clientName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      description: new FormControl('', {nonNullable: true, validators: [Validators.maxLength(500)]}),
      clientAuthenticationMethods: new FormControl<readonly string[]>([], {
        nonNullable: true,
        validators: [nonEmptyList],
      }),
      authorizationGrantTypes: new FormControl<readonly string[]>([], {
        nonNullable: true,
        validators: [nonEmptyList],
      }),
      scopes: new FormControl<readonly string[]>([], {nonNullable: true}),
      redirectUris: new FormArray<FormControl<string>>([]),
      postLogoutRedirectUris: new FormArray<FormControl<string>>([]),
      clientSettings: new FormGroup({
        requireProofKey: new FormControl(false, {nonNullable: true}),
        requireAuthorizationConsent: new FormControl(false, {nonNullable: true}),
        jwkSetUrl: new FormControl('', {nonNullable: true, validators: [uriValidator()]}),
        tokenEndpointAuthenticationSigningAlgorithm: new FormControl('', {nonNullable: true}),
        x509CertificateSubjectDN: new FormControl('', {nonNullable: true}),
        customSettings: newPairArray(),
      }),
      tokenSettings: new FormGroup({
        accessTokenTimeToLive: new FormControl<string | null>(null),
        refreshTokenTimeToLive: new FormControl<string | null>(null),
        authorizationCodeTimeToLive: new FormControl<string | null>(null),
        deviceCodeTimeToLive: new FormControl<string | null>(null),
        reuseRefreshTokens: new FormControl(false, {nonNullable: true}),
        x509CertificateBoundAccessTokens: new FormControl(false, {nonNullable: true}),
        idTokenSignatureAlgorithm: new FormControl('', {nonNullable: true}),
        accessTokenFormat: new FormControl('', {nonNullable: true}),
        customSettings: newPairArray(),
      }),
    },
    {validators: [clientShapeRules]},
  );

  /** Whatever `GET /{id}` answered, kept so the save can carry back what has no screen. */
  private readonly loadedClient = signal<ClientDetails | null>(null);

  private readonly loaded = snapshot(
    () => ({id: this.currentId()}),
    (query) =>
      forkJoin({
        client: query.id ? this.clients.findOne(query.id) : of(null),
        // A convenience, not the page: without it the form still edits, with empty option lists.
        options: this.clients.options().pipe(optionalOne()),
      }),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly reload = () => this.loaded.reload();
  readonly options = computed(() => this.loaded.value()?.options ?? EMPTY_OPTIONS);
  readonly isNew = computed(() => this.currentId() === null);
  /** uaa's status of the loaded client: type, enabled, the secret's lifetimes. Null while creating. */
  readonly status = computed(() => this.loadedClient()?.status ?? null);

  /** Bumped by every edit, so the readiness panel recomputes without polling the form. */
  private readonly revision = toSignal(this.form.valueChanges, {initialValue: null});

  readonly checks = computed<readonly ReadinessCheck[]>(() => {
    this.revision();
    return readiness(this.form);
  });

  readonly ready = computed(() => this.checks().every((check) => check.ok));

  readonly summary = computed(() => {
    this.revision();
    const raw = this.form.getRawValue();
    return {
      clientId: raw.clientId,
      auth: raw.clientAuthenticationMethods.join(', '),
      grants: raw.authorizationGrantTypes.join(', '),
      scopes: raw.scopes.join(', '),
      accessTtl: raw.tokenSettings.accessTokenTimeToLive ?? '',
      refreshTtl: raw.tokenSettings.refreshTokenTimeToLive ?? '',
      format: raw.tokenSettings.accessTokenFormat,
    };
  });

  constructor() {
    effect(() => {
      const value = this.loaded.value();
      if (value === undefined) {
        return;
      }
      this.loadedClient.set(value.client);
      this.fill(value.client);
    });
  }

  // ---------------------------------------------------------------- route

  back(): void {
    void this.router.navigate(['/clients']);
  }

  // ---------------------------------------------------------------- lists

  get redirectUris(): UriArray {
    return this.form.controls.redirectUris;
  }

  get postLogoutRedirectUris(): UriArray {
    return this.form.controls.postLogoutRedirectUris;
  }

  addUri(array: UriArray): void {
    array.push(new FormControl('', {nonNullable: true, validators: [uriValidator()]}));
    array.markAsDirty();
  }

  removeUri(array: UriArray, index: number): void {
    array.removeAt(index);
    array.markAsDirty();
    this.form.updateValueAndValidity();
  }

  addPair(array: PairArray): void {
    array.push(pairRow());
    array.markAsDirty();
  }

  removePair(array: PairArray, index: number): void {
    array.removeAt(index);
    array.markAsDirty();
  }

  // ---------------------------------------------------------------- multi-select

  /**
   * A checkbox group over a `string[]` control.
   *
   * `app-select` holds one value, and these are sets — which is exactly what `app-checkbox`
   * documents itself as being for. The control is written imperatively because the checkboxes are
   * a view over one control rather than one control each.
   */
  isPicked(control: FormControl<readonly string[]>, option: string): boolean {
    return control.value.includes(option);
  }

  togglePick(control: FormControl<readonly string[]>, option: string, picked: boolean): void {
    const next = picked
      ? [...control.value, option]
      : control.value.filter((entry) => entry !== option);
    control.setValue(next);
    control.markAsDirty();
    control.markAsTouched();
  }

  /** A client id suggested from the name, so nobody has to invent one that matches the pattern. */
  generateClientId(): void {
    const base = slugify(this.form.controls.clientName.value, 32) || 'client';
    const suffix = Math.random().toString(36).slice(2, 7);
    this.form.controls.clientId.setValue(`${base}-${suffix}`);
    this.form.controls.clientId.markAsDirty();
  }

  // ---------------------------------------------------------------- write

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.danger(this.transloco.translate('clientForm.invalid'));
      return;
    }
    const base = this.loadedClient();
    const body = this.toRequest(base);
    this.busy.set(true);
    if (base) {
      this.clients.update(base.id, body).subscribe({
        next: (saved) => {
          this.busy.set(false);
          this.toast.success(this.transloco.translate('clients.toast.updated', {clientId: body.clientId}));
          this.form.markAsPristine();
          this.loadedClient.set(saved);
        },
        error: (failure: unknown) => this.failed(failure),
      });
      return;
    }
    /*
     * Registration answers the generated secret exactly once. It is shown before the page moves to
     * the new client's route, and moving is what closing the dialog does — so the secret cannot be
     * lost behind a navigation the operator did not see.
     */
    this.clients.create(body).subscribe({
      next: (created) => {
        this.busy.set(false);
        this.form.markAsPristine();
        this.toast.success(this.transloco.translate('clients.toast.created', {clientId: body.clientId}));
        this.loadedClient.set(created.client);
        if (created.clientSecret) {
          this.shownSecret.set({
            clientId: created.client.clientId,
            secret: created.clientSecret,
            expiresAt: created.client.status?.clientSecretExpiresAt ?? null,
            previousUntil: null,
          });
        } else {
          void this.router.navigate(['/clients', created.client.id]);
        }
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** Closing the secret dialog after a registration lands on the new client's page. */
  dismissSecret(): void {
    const shown = this.shownSecret();
    this.shownSecret.set(null);
    const base = this.loadedClient();
    if (shown && base && this.currentId() === null) {
      void this.router.navigate(['/clients', base.id]);
    }
  }

  /**
   * A new random secret with a grace window for the old one. The console does not choose the secret:
   * uaa generates it, and this is the one moment it is readable.
   */
  rotateSecret(): void {
    const base = this.loadedClient();
    if (!base) {
      return;
    }
    this.busy.set(true);
    this.clients.rotateSecret(base.id).subscribe({
      next: (rotated: RotatedSecret) => {
        this.busy.set(false);
        this.rotating.set(false);
        this.toast.success(this.transloco.translate('clients.toast.secretRotated', {clientId: base.clientId}));
        this.shownSecret.set({
          clientId: rotated.clientId,
          secret: rotated.clientSecret,
          expiresAt: rotated.clientSecretExpiresAt,
          previousUntil: rotated.previousSecretUntil,
        });
        this.refresh(base.id);
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** Ends the grace window: the previous secret stops authenticating now. */
  revokePreviousSecret(): void {
    const base = this.loadedClient();
    if (!base) {
      return;
    }
    this.busy.set(true);
    this.clients.revokePreviousSecret(base.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.revokingPrevious.set(false);
        this.toast.success(this.transloco.translate('clients.toast.previousRevoked', {clientId: base.clientId}));
        this.refresh(base.id);
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** Disabling revokes every token the client holds; enabling puts it back at the token endpoint. */
  toggleEnabled(): void {
    const base = this.loadedClient();
    const status = this.status();
    if (!base || !status) {
      return;
    }
    this.busy.set(true);
    const call = status.enabled ? this.clients.disable(base.id) : this.clients.enable(base.id);
    call.subscribe({
      next: (saved) => {
        this.busy.set(false);
        this.loadedClient.set(saved);
        this.toast.success(
          this.transloco.translate(status.enabled ? 'clients.toast.disabled' : 'clients.toast.enabled', {
            clientId: base.clientId,
          }),
        );
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  /** Re-reads the status after a write that does not answer the whole client. */
  private refresh(id: string): void {
    this.clients.findOne(id).subscribe({next: (client) => this.loadedClient.set(client)});
  }

  confirmDelete(): void {
    const base = this.loadedClient();
    if (!base) {
      return;
    }
    this.busy.set(true);
    this.clients.delete(base.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.deleting.set(false);
        this.toast.success(this.transloco.translate('clients.toast.deleted', {clientId: base.clientId}));
        this.back();
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  // ---------------------------------------------------------------- mapping

  private fill(client: ClientDetails | null): void {
    setUris(this.redirectUris, client?.redirectUris ?? []);
    setUris(this.postLogoutRedirectUris, client?.postLogoutRedirectUris ?? []);
    setPairs(this.form.controls.clientSettings.controls.customSettings, client?.clientSettings.customSettings);
    setPairs(this.form.controls.tokenSettings.controls.customSettings, client?.tokenSettings.customSettings);

    this.form.patchValue({
      clientId: client?.clientId ?? '',
      clientName: client?.clientName ?? '',
      description: client?.status?.description ?? '',
      clientAuthenticationMethods: client?.clientAuthenticationMethods ?? ['client_secret_basic'],
      authorizationGrantTypes: client?.authorizationGrantTypes ?? ['authorization_code', 'refresh_token'],
      scopes: client?.scopes ?? ['openid'],
      clientSettings: {
        requireProofKey: client?.clientSettings.requireProofKey ?? false,
        requireAuthorizationConsent: client?.clientSettings.requireAuthorizationConsent ?? true,
        jwkSetUrl: client?.clientSettings.jwkSetUrl ?? '',
        tokenEndpointAuthenticationSigningAlgorithm:
          client?.clientSettings.tokenEndpointAuthenticationSigningAlgorithm ?? '',
        x509CertificateSubjectDN: client?.clientSettings.x509CertificateSubjectDN ?? '',
      },
      tokenSettings: {
        accessTokenTimeToLive: client?.tokenSettings.accessTokenTimeToLive ?? null,
        refreshTokenTimeToLive: client?.tokenSettings.refreshTokenTimeToLive ?? null,
        authorizationCodeTimeToLive: client?.tokenSettings.authorizationCodeTimeToLive ?? null,
        deviceCodeTimeToLive: client?.tokenSettings.deviceCodeTimeToLive ?? null,
        reuseRefreshTokens: client?.tokenSettings.reuseRefreshTokens ?? false,
        x509CertificateBoundAccessTokens: client?.tokenSettings.x509CertificateBoundAccessTokens ?? false,
        idTokenSignatureAlgorithm: client?.tokenSettings.idTokenSignatureAlgorithm ?? '',
        accessTokenFormat: client?.tokenSettings.accessTokenFormat?.value ?? '',
      },
    });
    this.form.markAsPristine();
  }

  private toRequest(base: ClientDetails | null): ClientDetails {
    const raw = this.form.getRawValue();
    const clientSettings: ClientSettings = {
      requireProofKey: raw.clientSettings.requireProofKey,
      requireAuthorizationConsent: raw.clientSettings.requireAuthorizationConsent,
      jwkSetUrl: blankToNull(raw.clientSettings.jwkSetUrl),
      tokenEndpointAuthenticationSigningAlgorithm: blankToNull(
        raw.clientSettings.tokenEndpointAuthenticationSigningAlgorithm,
      ),
      x509CertificateSubjectDN: blankToNull(raw.clientSettings.x509CertificateSubjectDN),
      customSettings: pairsToMap(raw.clientSettings.customSettings),
    };
    const tokenSettings: ClientTokenSettings = {
      accessTokenTimeToLive: raw.tokenSettings.accessTokenTimeToLive,
      refreshTokenTimeToLive: raw.tokenSettings.refreshTokenTimeToLive,
      authorizationCodeTimeToLive: raw.tokenSettings.authorizationCodeTimeToLive,
      deviceCodeTimeToLive: raw.tokenSettings.deviceCodeTimeToLive,
      reuseRefreshTokens: raw.tokenSettings.reuseRefreshTokens,
      x509CertificateBoundAccessTokens: raw.tokenSettings.x509CertificateBoundAccessTokens,
      idTokenSignatureAlgorithm: blankToNull(raw.tokenSettings.idTokenSignatureAlgorithm),
      /*
       * `OAuth2TokenFormat` serialises as `{value}`, not as the bare string the field name suggests.
       * Sending a string here is accepted and then read back as an object, so the next save would
       * carry `[object Object]` into the registry.
       */
      accessTokenFormat: raw.tokenSettings.accessTokenFormat
        ? {value: raw.tokenSettings.accessTokenFormat}
        : null,
      customSettings: pairsToMap(raw.tokenSettings.customSettings),
    };
    return {
      id: base?.id ?? '',
      clientId: raw.clientId.trim(),
      clientName: raw.clientName.trim(),
      clientAuthenticationMethods: raw.clientAuthenticationMethods,
      authorizationGrantTypes: raw.authorizationGrantTypes,
      redirectUris: cleanUris(raw.redirectUris),
      postLogoutRedirectUris: cleanUris(raw.postLogoutRedirectUris),
      scopes: raw.scopes,
      clientSettings,
      tokenSettings,
      // Only the description is writable here; the rest of the status is the server's.
      status: base?.status
        ? {...base.status, description: blankToNull(raw.description)}
        : {
            description: blankToNull(raw.description),
            enabled: true,
            type: 'CONFIDENTIAL',
            clientIdIssuedAt: null,
            clientSecretExpiresAt: null,
            lastTokenIssuedAt: null,
            disabledAt: null,
            disabledBy: null,
            previousSecretUntil: null,
          },
    };
  }

  private failed(failure: unknown): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, this.form);
    this.apiErrors.notify(failure);
  }
}

// -------------------------------------------------------------------- validators

/** A `string[]` control that has to hold at least one member. Reported as `required`, which the map already names. */
function nonEmptyList(control: {value: readonly string[] | null}): {required: true} | null {
  return (control.value?.length ?? 0) > 0 ? null : {required: true};
}

/**
 * The four rules that are about the *combination* of fields rather than any one of them.
 *
 * They live on the root group because each reads two branches at once, and they are the same four
 * the readiness panel lists — one source, so the panel cannot say ready while the form refuses.
 */
function clientShapeRules(group: object): Record<string, true> | null {
  const form = group as FormGroup;
  const raw = form.getRawValue() as ClientFormValue;
  const errors: Record<string, true> = {};
  if (needsRedirect(raw) && cleanUris(raw.redirectUris).length === 0) {
    errors['redirectUrisRequired'] = true;
  }
  if (isPublic(raw) && !raw.clientSettings.requireProofKey) {
    errors['proofKeyRequired'] = true;
  }
  if (raw.clientAuthenticationMethods.includes(AUTH_PRIVATE_KEY_JWT) && !raw.clientSettings.jwkSetUrl.trim()) {
    errors['jwkSetUrlRequired'] = true;
  }
  if (
    raw.clientAuthenticationMethods.includes(AUTH_TLS_CLIENT_AUTH) &&
    !raw.clientSettings.x509CertificateSubjectDN.trim()
  ) {
    errors['subjectDnRequired'] = true;
  }
  return Object.keys(errors).length ? errors : null;
}

interface ClientFormValue {
  readonly clientId: string;
  readonly clientName: string;
  readonly clientAuthenticationMethods: readonly string[];
  readonly authorizationGrantTypes: readonly string[];
  readonly redirectUris: readonly string[];
  readonly clientSettings: {
    readonly requireProofKey: boolean;
    readonly jwkSetUrl: string;
    readonly x509CertificateSubjectDN: string;
  };
}

const isPublic = (raw: ClientFormValue): boolean =>
  raw.clientAuthenticationMethods.length === 1 && raw.clientAuthenticationMethods[0] === AUTH_NONE;

const needsRedirect = (raw: ClientFormValue): boolean =>
  raw.authorizationGrantTypes.includes(GRANT_AUTHORIZATION_CODE);

/** The panel's lines, in the order they are worth reading. Same rules as {@link clientShapeRules}. */
function readiness(form: FormGroup): readonly ReadinessCheck[] {
  const raw = form.getRawValue() as ClientFormValue;
  return [
    {id: 'clientId', ok: CLIENT_ID_PATTERN.test(raw.clientId), key: 'clientForm.check.clientId'},
    {id: 'clientName', ok: raw.clientName.trim().length > 1, key: 'clientForm.check.clientName'},
    {
      id: 'auth',
      ok: raw.clientAuthenticationMethods.length > 0,
      key: 'clientForm.check.auth',
    },
    {id: 'grants', ok: raw.authorizationGrantTypes.length > 0, key: 'clientForm.check.grants'},
    {
      id: 'redirect',
      ok: !needsRedirect(raw) || cleanUris(raw.redirectUris).length > 0,
      key: needsRedirect(raw) ? 'clientForm.check.redirectRequired' : 'clientForm.check.redirectOptional',
    },
    {
      id: 'pkce',
      ok: !isPublic(raw) || raw.clientSettings.requireProofKey,
      key: isPublic(raw) ? 'clientForm.check.pkceRequired' : 'clientForm.check.pkceOptional',
    },
    {
      id: 'jwks',
      ok:
        !raw.clientAuthenticationMethods.includes(AUTH_PRIVATE_KEY_JWT) ||
        raw.clientSettings.jwkSetUrl.trim().length > 0,
      key: 'clientForm.check.jwkSetUrl',
    },
    {
      id: 'subjectDn',
      ok:
        !raw.clientAuthenticationMethods.includes(AUTH_TLS_CLIENT_AUTH) ||
        raw.clientSettings.x509CertificateSubjectDN.trim().length > 0,
      key: 'clientForm.check.subjectDn',
    },
  ];
}

// -------------------------------------------------------------------- array helpers

function setUris(array: UriArray, values: readonly string[]): void {
  array.clear({emitEvent: false});
  for (const value of values) {
    array.push(new FormControl(value, {nonNullable: true, validators: [uriValidator()]}), {
      emitEvent: false,
    });
  }
}

function setPairs(array: PairArray, map: Readonly<Record<string, unknown>> | undefined): void {
  array.clear({emitEvent: false});
  for (const [key, value] of Object.entries(map ?? {})) {
    array.push(pairRow(key, typeof value === 'string' ? value : JSON.stringify(value)), {
      emitEvent: false,
    });
  }
}

/** Blank rows are what an operator leaves behind after clicking Add; they are not a URI. */
function cleanUris(values: readonly string[]): readonly string[] {
  return values.map((value) => value.trim()).filter(Boolean);
}

function pairsToMap(
  pairs: readonly {key: string; value: string}[],
): Readonly<Record<string, unknown>> {
  const out: Record<string, unknown> = {};
  for (const pair of pairs) {
    const key = pair.key.trim();
    if (key) {
      out[key] = pair.value;
    }
  }
  return out;
}

const blankToNull = (value: string): string | null => (value.trim() ? value.trim() : null);
