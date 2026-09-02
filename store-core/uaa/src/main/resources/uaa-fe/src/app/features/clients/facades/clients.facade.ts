import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';
import {forkJoin} from 'rxjs';

import {ApiErrorService, optionalOne, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {
  AdminClientService,
  type ClientDetails,
  type ClientOptions,
  type ClientSummary,
} from '@cvhome-saas/ui-kit/uaa';

export const PAGE_SIZE = 20;

const EMPTY_OPTIONS: ClientOptions = {
  clientAuthenticationMethods: [],
  authorizationGrantTypes: [],
  scopes: [],
  idTokenSignatureAlgorithm: [],
  tokenEndpointAuthenticationSigningAlgorithm: [],
  accessTokenFormat: [],
};

/**
 * uaa's OAuth2 client registry.
 *
 * **The list and the detail are different shapes.** `GET /` answers `ClientSummary` —
 * `{id, clientId, clientName}` — so opening a client fetches it: grant types and scopes are simply
 * not in the list, and a table cannot show them without a request per row.
 *
 * **A secret is write-only.** Registration does not echo one and `reset-secret` answers `void`, so
 * the only moment anyone can read a secret is the one they typed it in. The form says so, because an
 * operator who assumes they can look it up later will lock a service out of the platform.
 *
 * **The option lists come from the server**, built from `ClientAuthMethod`, `OAuthGrantType` and
 * `SignatureAlgorithm`, so this form cannot offer a grant type uaa would reject. `optionalOne` wraps
 * that leg: a failure there leaves the form usable with the values the client already has, whereas a
 * failed `forkJoin` would render a blank page.
 */
@Injectable()
export class ClientsFacade {
  private readonly clients = inject(AdminClientService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly pageIndex = signal(0);

  /**
   * What the detail pane is showing: a fetched client, `'new'` while registering, or null.
   *
   * The list carries three fields, so selecting a row *fetches* — `ClientSummary` has no grant
   * types, scopes or redirect URIs to show.
   */
  readonly selected = signal<ClientDetails | 'new' | null>(null);
  readonly deleting = signal<ClientSummary | null>(null);
  readonly rotating = signal<ClientSummary | null>(null);

  readonly form = new FormGroup({
    clientId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    clientName: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    redirectUris: new FormControl('', {nonNullable: true}),
    postLogoutRedirectUris: new FormControl('', {nonNullable: true}),
    scopes: new FormControl('', {nonNullable: true}),
    authorizationGrantTypes: new FormControl('', {nonNullable: true}),
    clientAuthenticationMethods: new FormControl('', {nonNullable: true}),
    /*
     * ISO-8601 durations, because that is what Java's `Duration` serialises to and what uaa reads
     * back — `PT30M`, `P30D`. Taken verbatim rather than as a number plus a unit picker: the server
     * accepts the whole grammar and inventing a smaller one here would reject valid values.
     */
    accessTokenTimeToLive: new FormControl('', {nonNullable: true}),
    refreshTokenTimeToLive: new FormControl('', {nonNullable: true}),
  });

  private readonly page = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE}),
    (query) =>
      forkJoin({
        clients: this.clients.list(query.page, query.count),
        // The option lists are a convenience, not the page: if they fail the form still edits the
        // values a client already has, whereas a failed forkJoin would render nothing at all.
        options: this.clients.options().pipe(optionalOne()),
      }),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly isEmpty = this.page.isEmpty;
  readonly reload = () => this.page.reload();

  readonly rows = computed(() => this.page.value()?.clients.content ?? []);
  readonly totalElements = computed(() => this.page.value()?.clients.totalElements ?? 0);
  readonly totalPages = computed(() => this.page.value()?.clients.totalPages ?? 0);
  readonly options = computed(() => this.page.value()?.options ?? EMPTY_OPTIONS);

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  dismiss(): void {
    this.selected.set(null);
    this.deleting.set(null);
    this.rotating.set(null);
  }

  /** The row currently in the pane, for the list's selected state. */
  isSelected(row: ClientSummary): boolean {
    const target = this.selected();
    return target !== null && target !== 'new' && target.id === row.id;
  }

  startCreate(): void {
    this.form.reset({
      clientId: '',
      clientName: '',
      redirectUris: '',
      postLogoutRedirectUris: '',
      scopes: 'openid, profile',
      authorizationGrantTypes: 'authorization_code, refresh_token',
      clientAuthenticationMethods: 'client_secret_basic',
      accessTokenTimeToLive: '',
      refreshTokenTimeToLive: '',
    });
    this.selected.set('new');
  }

  /** Opens the pane on a row, which needs a fetch: the list carries three fields. */
  select(row: ClientSummary): void {
    this.busy.set(true);
    this.clients.findOne(row.id).subscribe({
      next: (client) => {
        this.busy.set(false);
        this.form.reset({
          clientId: client.clientId,
          clientName: client.clientName,
          redirectUris: client.redirectUris.join(', '),
          postLogoutRedirectUris: client.postLogoutRedirectUris.join(', '),
          scopes: client.scopes.join(', '),
          authorizationGrantTypes: client.authorizationGrantTypes.join(', '),
          clientAuthenticationMethods: client.clientAuthenticationMethods.join(', '),
          accessTokenTimeToLive: client.tokenSettings?.accessTokenTimeToLive ?? '',
          refreshTokenTimeToLive: client.tokenSettings?.refreshTokenTimeToLive ?? '',
        });
        this.selected.set(client);
      },
      error: (failure: unknown) => this.failed(failure),
    });
  }

  save(): void {
    const target = this.selected();
    if (!target || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const base = target === 'new' ? null : target;
    const body: ClientDetails = {
      id: base?.id ?? '',
      clientId: raw.clientId.trim(),
      clientName: raw.clientName.trim(),
      clientAuthenticationMethods: list(raw.clientAuthenticationMethods),
      authorizationGrantTypes: list(raw.authorizationGrantTypes),
      redirectUris: list(raw.redirectUris),
      postLogoutRedirectUris: list(raw.postLogoutRedirectUris),
      scopes: list(raw.scopes),
      // Settings are not edited here: they are two records of protocol detail, and getting one wrong
      // breaks an integration in a way no screen in this console would show. Whatever the client
      // already has is preserved; a new one takes uaa's defaults.
      clientSettings: base?.clientSettings ?? {
        requireProofKey: false,
        requireAuthorizationConsent: true,
        jwkSetUrl: null,
        tokenEndpointAuthenticationSigningAlgorithm: null,
        x509CertificateSubjectDN: null,
        customSettings: {},
      },
      /*
       * The two lifetimes are edited; everything else in `tokenSettings` is protocol detail with no
       * screen, so it is carried through untouched rather than reset to a default that would
       * silently change how a live client behaves.
       */
      tokenSettings: {
        ...(base?.tokenSettings ?? {
          authorizationCodeTimeToLive: null,
          accessTokenFormat: null,
          deviceCodeTimeToLive: null,
          reuseRefreshTokens: false,
          idTokenSignatureAlgorithm: null,
          x509CertificateBoundAccessTokens: false,
          customSettings: {},
        }),
        accessTokenTimeToLive: raw.accessTokenTimeToLive.trim() || null,
        refreshTokenTimeToLive: raw.refreshTokenTimeToLive.trim() || null,
      },
    };
    this.busy.set(true);
    const call = base ? this.clients.update(base.id, body) : this.clients.create(body);
    call.subscribe({
      next: () => this.settled(base ? 'clients.toast.updated' : 'clients.toast.created', body.clientId),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  rotateSecret(secret: string): void {
    const target = this.rotating();
    if (!target) {
      return;
    }
    this.busy.set(true);
    this.clients.resetSecret(target.id, secret).subscribe({
      next: () => this.settled('clients.toast.secretRotated', target.clientId),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  confirmDelete(): void {
    const target = this.deleting();
    if (!target) {
      return;
    }
    this.busy.set(true);
    this.clients.delete(target.id).subscribe({
      next: () => this.settled('clients.toast.deleted', target.clientId),
      error: (failure: unknown) => this.failed(failure),
    });
  }

  private settled(key: string, clientId: string): void {
    this.busy.set(false);
    this.dismiss();
    this.toast.success(this.transloco.translate(key, {clientId}));
    this.page.reload();
  }

  private failed(failure: unknown): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, this.form);
    this.apiErrors.notify(failure);
  }
}

/** `a, b , c` -> `['a','b','c']`. Commas because these are short, familiar protocol tokens. */
function list(raw: string): readonly string[] {
  return raw
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean);
}
