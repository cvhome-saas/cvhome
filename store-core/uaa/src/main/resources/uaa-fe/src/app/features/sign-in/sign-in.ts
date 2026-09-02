import {Component, inject, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
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
  protected readonly csrf = signal(readCookie('XSRF-TOKEN'));

  /** Which message to show above the form, keyed into `signIn.*`, or `undefined` for none. */
  protected readonly notice = signal<'failed' | 'expired' | 'signedOut' | undefined>(readNotice());

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

function readNotice(): 'failed' | 'expired' | 'signedOut' | undefined {
  const params = new URLSearchParams(window.location.search);
  if (params.has('logout')) {
    return 'signedOut';
  }
  if (!params.has('error')) {
    return undefined;
  }
  return params.get('error') === 'expired' ? 'expired' : 'failed';
}
