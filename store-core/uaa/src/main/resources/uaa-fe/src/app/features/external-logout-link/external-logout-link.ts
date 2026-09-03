import {DOCUMENT} from '@angular/common';
import {Component, OnInit, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

/**
 * The logout bounce, as console-ui has it.
 *
 * The kit's `AuthService.logout()` navigates to this route rather than to `/logout` directly, so the
 * app decides where its session ends. Here that is uaa's own `/logout`, which invalidates the session
 * and returns to `/login?logout`. Without this route the navigation fell through to the catch-all and
 * Sign out did nothing.
 *
 * It **posts** rather than navigates, and that is the point: a `/logout` reachable by GET is a link or
 * an `<img>` on any site that signs the visitor out of this one. uaa's matcher is POST-only, so the
 * CSRF token has to travel with it — read from the `XSRF-TOKEN` cookie at submit, exactly as the
 * sign-in form does. A form rather than `fetch` because the response is a redirect that the browser
 * should follow.
 */
@Component({
  selector: 'app-external-logout-link',
  imports: [TranslocoDirective],
  template: `<main class="logout" *transloco="let t"><p role="status">{{ t('signIn.signingOut') }}</p></main>`,
  styles: `
    .logout {
      display: grid;
      place-content: center;
      min-block-size: 100vh;
      background: var(--background);
      color: var(--muted-foreground);
    }
  `,
})
export class ExternalLogoutLink implements OnInit {
  private readonly document = inject(DOCUMENT);
  private readonly config = inject(UI_KIT_CONFIG);

  ngOnInit(): void {
    if (!this.document.defaultView) {
      return;
    }
    const form = this.document.createElement('form');
    form.method = 'post';
    form.action = this.config.logoutUrl;
    const token = readCookie(this.document, 'XSRF-TOKEN');
    if (token) {
      const csrf = this.document.createElement('input');
      csrf.type = 'hidden';
      csrf.name = '_csrf';
      csrf.value = token;
      form.appendChild(csrf);
    }
    this.document.body.appendChild(form);
    form.submit();
  }
}

function readCookie(document: Document, name: string): string {
  const match = document.cookie
    .split('; ')
    .map((pair) => pair.split('='))
    .find(([key]) => key === name);
  return match ? decodeURIComponent(match.slice(1).join('=')) : '';
}
