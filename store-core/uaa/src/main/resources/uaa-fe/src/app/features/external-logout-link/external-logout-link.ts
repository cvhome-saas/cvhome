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
    const view = this.document.defaultView;
    if (view) {
      view.location.href = this.config.logoutUrl;
    }
  }
}
