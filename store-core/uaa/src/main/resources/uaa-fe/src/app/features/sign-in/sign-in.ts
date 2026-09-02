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
 * CSRF is disabled in uaa (`csrf(AbstractHttpConfigurer::disable)`), so there is no hidden token to
 * carry. If that ever changes, this form needs one and will fail loudly with a 403 until it has it.
 *
 * `?error` is what `formLogin` appends on a failed attempt; it is the only signal the page gets, and
 * deliberately says nothing about which half was wrong.
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
  protected readonly failed = new URLSearchParams(window.location.search).has('error');
  protected readonly submitting = signal(false);

  protected onSubmit(): void {
    this.submitting.set(true);
  }
}
