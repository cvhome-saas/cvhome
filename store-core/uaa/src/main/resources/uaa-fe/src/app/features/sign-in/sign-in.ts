import {Component, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {catchError, of} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {CrudService, UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import {FormField, Icon, Panel, TextField} from '@cvhome-saas/ui-kit/ui';

/**
 * The platform's sign-in page.
 *
 * **This is a native form POST, not an API call.** `AppSecurityConfig` declares
 * `formLogin(loginPage("/login"))`, so the browser must submit `username` and `password` to `/login`
 * as a real form and let Spring Security answer with a redirect. Posting it with `HttpClient`
 * instead would strand the OAuth2 authorization flow: the redirect that resumes it is the response.
 *
 * That is why the controls carry `name` — a browser only submits inputs that have one — and why
 * there is no reactive form here. The one thing this page does in TypeScript is disable the button
 * on submit, so a slow round trip cannot be double-posted.
 *
 * CSRF is on in uaa, with `CookieCsrfTokenRepository`: the response that served this page planted an
 * `XSRF-TOKEN` cookie, and the form carries it back as the hidden `_csrf` field. The cookie is read
 * at render time, not at submit, so a tab left open past the session posts a stale token and comes
 * back as `?error=expired` — which is the one case where the page says more than "did not match".
 *
 * `?error` is what `formLogin` appends on a failed attempt; it deliberately says nothing about which
 * half was wrong. `?logout` is what `/logout` appends when it is done.
 */
@Component({
  selector: 'app-sign-in',
  imports: [TranslocoDirective, FormField, TextField, Panel, Icon],
  templateUrl: './sign-in.html',
  styleUrl: './sign-in.css',
})
export class SignIn {
  private readonly config = inject(UI_KIT_CONFIG);

  protected readonly action = this.config.loginUrl;
  protected readonly submitting = signal(false);
  private readonly http = inject(CrudService);

  protected readonly csrf = signal(readCookie('XSRF-TOKEN'));

  /** Which message to show above the form, keyed into `signIn.*`, or `undefined` for none. */
  protected readonly notice = signal<Notice | undefined>(readNotice());

  /** How many attempts are left before a lock, when the server said. */
  protected readonly attemptsLeft = readAttemptsLeft();

  /**
   * What the realm lets this page offer. Public, and allowed to fail: without it the form is the plain
   * username/password one, which is always right.
   */
  protected readonly realm = toSignal(
    this.http.get<LoginSettings>('/api/v1/public/login/settings').pipe(catchError(() => of(null))),
    {initialValue: null as LoginSettings | null},
  );

  protected readonly rememberMe = signal(false);

  protected onSubmit(): void {
    // Re-read at the last moment: the cookie may have been rotated since the page rendered.
    this.csrf.set(readCookie('XSRF-TOKEN'));
    this.submitting.set(true);
  }
}

function readCookie(name: string): string {
  const match = document.cookie
    .split('; ')
    .map((pair) => pair.split('='))
    .find(([key]) => key === name);
  return match ? decodeURIComponent(match.slice(1).join('=')) : '';
}

type Notice = 'failed' | 'expired' | 'signedOut' | 'locked' | 'disabled' | 'expiredPassword';

/** What `GET /api/v1/public/login/settings` answers. */
interface LoginSettings {
  readonly displayName: string;
  readonly rememberMeEnabled: boolean;
  readonly lockoutThreshold: number;
  readonly lockoutMinutes: number;
}

/**
 * The failure handler's vocabulary: `?error` alone is a wrong password, `?error=locked|disabled|expired-password`
 * are states the person needs to know about, `?error=expired` is a stale CSRF token, `?logout` is a farewell.
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
      return 'locked';
    case 'disabled':
      return 'disabled';
    case 'expired-password':
      return 'expiredPassword';
    default:
      return 'failed';
  }
}

function readAttemptsLeft(): number | null {
  const value = new URLSearchParams(window.location.search).get('attemptsLeft');
  return value === null ? null : Number.parseInt(value, 10);
}
