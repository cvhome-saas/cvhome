import {DOCUMENT} from '@angular/common';
import {Component, OnInit, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {RouterLink} from '@angular/router';
import {catchError, of} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {ApiErrorService, AuthService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import {FormField, Icon, TextField} from '@cvhome-saas/ui-kit/ui';
import {PublicLoginService, type PublicIdpDto} from '@cvhome-saas/ui-kit/uaa';
import {AuthStory} from '../components/auth-story';
import {AuthFacade} from '../facades/auth.facade';

/**
 * The console's own sign-in page, and the only one a merchant sees.
 *
 * <p>
 * It used to be a button that said "Continue with cvhome UAA" and handed the browser to uaa, which rendered the
 * real form on its own host. The console owns the form now — the same arrangement the storefront has with cua —
 * so a merchant never leaves this origin to sign in. uaa still authenticates; what moved is only the page.
 * </p>
 *
 * **Nothing renders until it knows whether anyone is signed in.** Asking that first is the whole reason this
 * page has a waiting state: a session that is already good ends here as a redirect into the console, and drawing
 * a sign-in form on the way — for a person who is signed in and is about to be sent onward — is a form that was
 * never true. The check is one call to the gateway's `/api/v1/auth/me`, which answers for the *gateway's*
 * session; that is the one the console rides on.
 *
 * **Then two halves, which cannot loop.** Without uaa's `?auth=1` marker there is no saved authorization to
 * answer, so this starts one and the browser leaves. With the marker uaa is holding a request and waiting for
 * credentials, so the form renders. Neither half redirects in the other's case, which is what stops the bounce.
 *
 * **One step, not two.** uaa's own console asks for an address first and discovers the provider behind it; this
 * page asks for both at once, which is what the console's design calls for and what a merchant with a password
 * expects. The provider buttons are still here, so an SSO merchant never types a password at all.
 *
 * **The password step is a native form POST**, not an `HttpClient` call. uaa declares
 * `formLogin(loginPage("/login"))`, so the browser has to post `username` and `password` as a real form and let
 * Spring Security answer with a redirect — that redirect is what resumes the authorization the gateway started.
 * Posting it with `HttpClient` would strand the flow. Hence `name` attributes and no reactive form.
 *
 * CSRF is on: the redirect that sent the browser here planted an `XSRF-TOKEN` cookie at `Path=/`, and the form
 * carries it back as `_csrf`. It is read at submit rather than at render, so a tab left open past a rotation
 * still posts the current token; a form posted without one comes back as `error=expired` with a fresh cookie.
 */
@Component({
  selector: 'app-sign-in',
  imports: [AuthStory, RouterLink, TranslocoDirective, FormField, TextField, Icon],
  templateUrl: './sign-in.html',
  styleUrl: '../auth.css',
})
export class SignIn implements OnInit {
  private readonly document = inject(DOCUMENT);
  private readonly config = inject(UI_KIT_CONFIG);
  private readonly login = inject(PublicLoginService);
  private readonly auth = inject(AuthService);
  private readonly apiErrors = inject(ApiErrorService);

  protected readonly facade = inject(AuthFacade);

  /** uaa's form-login processing URL, on this origin, behind the gateway's forward prefix. */
  protected readonly action = `${this.config.uaaBasePath}/login`;

  /** True while the answer to "is anybody already signed in?" is still outstanding. Nothing draws until it is. */
  protected readonly resolving = signal(true);

  /** True once uaa has said it is holding an authorization request; until then this page starts one. */
  protected readonly pendingFlow = signal(false);

  protected readonly submitting = signal(false);
  protected readonly csrf = signal('');

  /** `link` is the one detour: a brokered login that matched an existing account, confirming it once. */
  protected readonly step = signal<'credentials' | 'link'>('credentials');

  protected readonly email = signal('');
  protected readonly rememberMe = signal(false);

  /**
   * "Forgot password?" opens an explanation, not a form. uaa has no self-service reset: an administrator issues
   * a one-time link and the person follows it. Saying so is more use than a form that cannot work.
   */
  protected readonly forgotOpen = signal(false);
  protected readonly linkPassword = signal('');
  protected readonly linkError = signal<string | null>(null);
  protected readonly notice = signal<Notice | undefined>(undefined);
  protected readonly attemptsLeft = signal<number | null>(null);

  /**
   * What the realm lets this page offer. Public, and allowed to fail: without it the form is the plain
   * username and password one, which is always right.
   */
  protected readonly realm = toSignal(this.login.settings().pipe(catchError(() => of(null))), {initialValue: null});

  protected readonly providers = toSignal(
    this.login.providers().pipe(catchError(() => of([] as readonly PublicIdpDto[]))),
    {initialValue: [] as readonly PublicIdpDto[]},
  );

  protected readonly context = toSignal(this.login.context().pipe(catchError(() => of(null))), {initialValue: null});

  protected readonly pending = computed(() => this.context()?.pendingLink ?? null);

  ngOnInit(): void {
    const view = this.document.defaultView;
    if (!view) {
      return;
    }
    /*
     * Ask before drawing. `getAuthUser` answers from the gateway's session and raises a typed
     * `SESSION_MISSING` when there is none — the endpoint returns 200 with an empty body either way, which is
     * why the failure path here is the *normal* one for somebody who has come to sign in.
     */
    this.auth.getAuthUser().subscribe({
      next: () => view.location.assign(SIGNED_IN_HOME),
      error: () => this.startOrRenderTheForm(view),
    });
  }

  private startOrRenderTheForm(view: Window): void {
    const params = new URLSearchParams(view.location.search);
    if (!params.has(PENDING_PARAM)) {
      // Nothing saved on uaa's side, so there is nothing to render a form for: start the flow instead.
      view.location.href = this.config.loginUrl;
      return;
    }
    this.pendingFlow.set(true);
    this.resolving.set(false);
    this.csrf.set(readCookie(this.document, CSRF_COOKIE));
    this.notice.set(readNotice(params));
    this.attemptsLeft.set(readAttemptsLeft(params));
    queueMicrotask(() => {
      if (this.pending()) {
        this.step.set('link');
      }
    });
  }

  /** One glyph per provider, the way every sign-in page draws them. */
  protected glyph(provider: PublicIdpDto): string {
    switch (provider.preset) {
      case 'GOOGLE':
        return 'G';
      case 'MICROSOFT':
        return 'M';
      case 'APPLE':
        return 'A';
      case 'GITHUB':
        return 'GH';
      default:
        return 'ID';
    }
  }

  /**
   * Where a provider button goes, carrying the typed address as a hint.
   *
   * `authorizationUrl` is relative to uaa's own origin, so it needs the gateway's forward prefix in front of it
   * here — uaa serves that page itself, the console does not.
   */
  protected providerUrl(provider: PublicIdpDto): string {
    const url = `${this.config.uaaBasePath}${provider.authorizationUrl}`;
    const hint = this.email().trim();
    return hint ? `${url}?login_hint=${encodeURIComponent(hint)}` : url;
  }

  protected onSubmit(): void {
    // Re-read at the last moment: the cookie may have rotated since the page rendered.
    this.csrf.set(readCookie(this.document, CSRF_COOKIE));
    this.submitting.set(true);
  }

  /** The password step of a brokered login that matched an account; answers where to go, so the page navigates. */
  protected confirmLink(event: Event): void {
    event.preventDefault();
    const password = this.linkPassword();
    if (!password) {
      return;
    }
    this.submitting.set(true);
    this.linkError.set(null);
    this.login.confirmLink(password).subscribe({
      next: ({redirectTo}) => this.document.defaultView?.location.assign(redirectTo),
      error: (failure: unknown) => {
        this.submitting.set(false);
        this.linkError.set(this.apiErrors.messageFor(failure));
      },
    });
  }
}

/** uaa's marker: it is holding a saved authorization request, so render the form rather than starting one. */
const PENDING_PARAM = 'auth';

/**
 * Where somebody who is already signed in is sent instead of being shown a form.
 *
 * The console's home rather than this page's own route: the guards there send a platform operator to
 * `/platform` and a merchant with no store yet to `getting-started`, so one target covers every account.
 */
const SIGNED_IN_HOME = '/dashboard';

const CSRF_COOKIE = 'XSRF-TOKEN';

function readCookie(document: Document, name: string): string {
  const match = document.cookie
    .split('; ')
    .map((pair) => pair.split('='))
    .find(([key]) => key === name);
  return match ? decodeURIComponent(match.slice(1).join('=')) : '';
}

type Notice =
  | 'failed'
  | 'expired'
  | 'signedOut'
  | 'locked'
  | 'disabled'
  | 'expiredPassword'
  | 'idpRejected'
  | 'idpUnknownUser'
  | 'idpNoEmail'
  | 'idpFailed';

/**
 * uaa's failure vocabulary, unchanged.
 *
 * The console reads exactly what uaa's own page reads, because the redirect that carries it is the same one:
 * `?error` alone is a wrong password, `locked`, `disabled` and `expired-password` are states the person has to
 * know about, `expired` is a stale CSRF token, the `idp_*` codes are a brokered login that did not complete, and
 * `?logout` is a farewell. `link_required` is not a failure — it is the confirmation step below.
 */
function readNotice(params: URLSearchParams): Notice | undefined {
  if (params.has('logout')) {
    return 'signedOut';
  }
  if (!params.has('error')) {
    return undefined;
  }
  switch (params.get('error')) {
    case 'expired':
      return 'expired';
    case 'locked':
    case 'account_locked':
      return 'locked';
    case 'disabled':
    case 'account_disabled':
      return 'disabled';
    case 'expired-password':
      return 'expiredPassword';
    case 'idp_rejected':
      return 'idpRejected';
    case 'idp_unknown_user':
      return 'idpUnknownUser';
    case 'idp_no_email':
      return 'idpNoEmail';
    case 'idp':
    case 'idp_unknown':
      return 'idpFailed';
    case 'link_required':
      return undefined;
    default:
      return 'failed';
  }
}

function readAttemptsLeft(params: URLSearchParams): number | null {
  const value = params.get('attemptsLeft');
  return value === null ? null : Number.parseInt(value, 10);
}
