import {DOCUMENT} from '@angular/common';
import {Component, OnInit, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';

/**
 * The logout bounce.
 *
 * A route rather than a direct link because signing out is the gateway's job, not the router's: `AuthService.logout()`
 * navigates here and this hands the browser to `/logout`, which clears the session and returns. Without it that
 * navigation resolved to nothing and the seller stayed signed in — the console had a Log out that did not log out.
 */
@Component({
  selector: 'app-external-logout-link',
  imports: [TranslocoDirective],
  template: `<main class="logout" *transloco="let t"><p role="status">{{ t('auth.signingOut') }}</p></main>`,
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
    const view = this.document.defaultView;
    if (view) {
      view.location.href = this.config.logoutUrl;
    }
  }
}
