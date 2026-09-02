import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {SubscriptionFacade} from '@layouts/console-shell/billing/subscription.facade';
import {Icon} from '@cvhome-saas/ui-kit/ui';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/**
 * The plan notice pinned above the console — when there is one.
 *
 * **It used to be a constant.** The banner said "You're on the Free plan — upgrade to add more stores"
 * to every operator on every page, whatever they were paying, and its Upgrade button did nothing. A
 * paying customer was told to upgrade; a store whose card had just failed was told nothing.
 *
 * It now renders `SubscriptionFacade.banner()`, which is `null` for a healthy paid store — so the common
 * case is **no banner at all**. What remains is a trial running down, a payment that failed, a
 * cancellation already scheduled, or a store billing has not caught up with yet. `Upgrade` goes to the
 * billing page (`/subscription` — the gateway owns `/billing`) rather than opening a checkout from the chrome: choosing what to pay for is a decision
 * with a price attached, and it belongs on a page that shows the plans.
 */
@Component({
  selector: 'app-plan-banner',
  imports: [Icon, RouterLink, TranslocoDirective],
  template: `
    @if (billing.banner(); as banner) {
      <aside
        class="plan-banner"
        [class.warn]="banner.tone === 'warn'"
        [class.danger]="banner.tone === 'danger'"
        [attr.aria-label]="t('shell.planBanner.ariaLabel')"
        *transloco="let t"
      >
        <app-icon [name]="banner.tone === 'info' ? 'sparkles' : 'alertCircle'" />
        <p>{{ banner.message }}</p>

        @if (banner.canUpgrade) {
          <a class="upgrade" routerLink="/subscription">
            {{ t('shell.planBanner.upgrade') }} <app-icon name="arrowUpRight" [flip]="true" />
          </a>
        } @else {
          <a class="upgrade quiet" routerLink="/subscription">
            {{ t('shell.planBanner.manage') }} <app-icon name="arrowUpRight" [flip]="true" />
          </a>
        }

        <button
          class="dismiss"
          type="button"
          [attr.aria-label]="t('shell.planBanner.dismiss')"
          (click)="shell.bannerVisible.set(false)"
        >
          <app-icon name="x" />
        </button>
      </aside>
    }
  `,
  styleUrl: './plan-banner.css',
})
export class PlanBanner {
  protected readonly shell = inject(ConsoleShellFacade);
  protected readonly billing = inject(SubscriptionFacade);
}
