import {Component, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {catchError, of} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {ApiErrorService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import {FormField, Icon, Panel, TextField} from '@cvhome-saas/ui-kit/ui';
import {PublicLoginService, type PublicIdpDto} from '@cvhome-saas/ui-kit/uaa';

/**
 * The platform's sign-in page, identity-first.
 *
 * **Step one takes an email or a username**, and what happens next depends on the realm: an address at a provider's
 * domain goes straight to that provider, and everything else reaches the password step with the identity shown above
 * it and carried into the username field — which is what uaa authenticates, so a person whose username is not their
 * address can correct it there. Nothing here says whether an account exists: discovery answers a provider or nothing,
 * and the password step looks the same either way.
 *
 * **The password step is still a native form POST**, not an API call. `AppSecurityConfig` declares
 * `formLogin(loginPage("/login"))`, so the browser must submit `username` and `password` to `/login` as a real form
 * and let Spring Security answer with a redirect: that redirect is what resumes the OAuth2 authorization the console
 * started. Posting it with `HttpClient` would strand the flow. Hence `name` attributes and no reactive form.
 *
 * CSRF is on, with `CookieCsrfTokenRepository`: the response that served this page planted an `XSRF-TOKEN` cookie and
 * the form carries it back as `_csrf`. The cookie is read at submit rather than at render, so a tab left open past a
 * session rotation still posts the current token.
 *
 * **`link_required` is not a failure.** A brokered login whose email matches an existing account lands back here to
 * confirm with that account's password once; the page says whose account and which provider, and posts to
 * `/api/v1/auth/link-confirm` — which is an API call, because it answers where to go rather than redirecting.
 */
@Component({
  selector: 'app-sign-in',
  imports: [TranslocoDirective, FormField, TextField, Panel, Icon],
  templateUrl: './sign-in.html',
  styleUrl: './sign-in.css',
})
export class SignIn {
  private readonly config = inject(UI_KIT_CONFIG);
  private readonly login = inject(PublicLoginService);
  private readonly apiErrors = inject(ApiErrorService);

  protected readonly action = this.config.loginUrl;
  protected readonly submitting = signal(false);
  protected readonly csrf = signal(readCookie('XSRF-TOKEN'));

  /** Which step the page is on. `link` is the confirmation a brokered login asked for. */
  protected readonly step = signal<'identity' | 'password' | 'link'>(readNotice() ? 'password' : 'identity');
  protected readonly email = signal('');
  protected readonly checking = signal(false);
  protected readonly rememberMe = signal(false);
  protected readonly linkPassword = signal('');
  protected readonly linkError = signal<string | null>(null);

  /**
   * "Forgot password?" opens an explanation, not a form. There is no self-service reset: an administrator issues a
   * one-time link from the console, and the person follows it.
   */
  protected readonly forgotOpen = signal(false);

  /** Which message to show above the form, keyed into `signIn.*`, or `undefined` for none. */
  protected readonly notice = signal<Notice | undefined>(readNotice());

  /** How many attempts are left before a lock, when the server said. */
  protected readonly attemptsLeft = readAttemptsLeft();

  /**
   * What the realm lets this page offer. Public, and allowed to fail: without it the form is the plain
   * username/password one, which is always right.
   */
  protected readonly realm = toSignal(this.login.settings().pipe(catchError(() => of(null))), {initialValue: null});

  /** The provider buttons, in the order an administrator put them. */
  protected readonly providers = toSignal(this.login.providers().pipe(catchError(() => of([] as readonly PublicIdpDto[]))), {
    initialValue: [] as readonly PublicIdpDto[],
  });

  /** Why this page is being shown: which client asked, and any brokered login waiting for its password. */
  protected readonly context = toSignal(this.login.context().pipe(catchError(() => of(null))), {initialValue: null});

  protected readonly pending = computed(() => this.context()?.pendingLink ?? null);

  constructor() {
    // A brokered login that matched an account lands here to confirm it; that is the step, whatever the query says.
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

  /** Where a provider button goes, carrying the email as a hint when the visitor typed one. */
  protected providerUrl(provider: PublicIdpDto): string {
    const hint = this.email().trim();
    return hint ? `${provider.authorizationUrl}?login_hint=${encodeURIComponent(hint)}` : provider.authorizationUrl;
  }

  /**
   * Step one: ask uaa whether this address belongs to a provider. A match navigates there; anything else — including
   * a failure — moves to the password step, which is what a person with a local account needs.
   */
  protected continueWithEmail(event: Event): void {
    event.preventDefault();
    const email = this.email().trim();
    if (!email) {
      return;
    }
    this.checking.set(true);
    this.login.discover(email).subscribe({
      next: ({provider}) => {
        this.checking.set(false);
        if (provider) {
          window.location.assign(this.providerUrl(provider));
          return;
        }
        this.step.set('password');
      },
      error: () => {
        this.checking.set(false);
        this.step.set('password');
      },
    });
  }

  protected back(): void {
    this.step.set('identity');
    this.notice.set(undefined);
  }

  protected onSubmit(): void {
    // Re-read at the last moment: the cookie may have been rotated since the page rendered.
    this.csrf.set(readCookie('XSRF-TOKEN'));
    this.submitting.set(true);
  }

  /** The password step of a brokered login: answers where to go, so the page navigates rather than the server. */
  protected confirmLink(event: Event): void {
    event.preventDefault();
    const password = this.linkPassword();
    if (!password) {
      return;
    }
    this.submitting.set(true);
    this.linkError.set(null);
    this.login.confirmLink(password).subscribe({
      next: ({redirectTo}) => window.location.assign(redirectTo),
      error: (failure: unknown) => {
        this.submitting.set(false);
        this.linkError.set(this.apiErrors.messageFor(failure));
      },
    });
  }
}

function readCookie(name: string): string {
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
 * The failure handlers' vocabulary. `?error` alone is a wrong password; `locked`, `disabled` and `expired-password`
 * are states the person must know about; `expired` is a stale CSRF token; the `idp_*` codes are a brokered login that
 * did not complete; `?logout` is a farewell.
 */
function readNotice(): Notice | undefined {
  const params = new URLSearchParams(window.location.search);
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

function readAttemptsLeft(): number | null {
  const value = new URLSearchParams(window.location.search).get('attemptsLeft');
  return value === null ? null : Number.parseInt(value, 10);
}
