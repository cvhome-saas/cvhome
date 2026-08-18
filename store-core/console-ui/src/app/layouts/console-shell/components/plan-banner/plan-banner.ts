import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';

/** Dismissible plan notice pinned above the console. */
@Component({
  selector: 'app-plan-banner',
  imports: [Icon, TranslocoDirective],
  template: `
    <aside class="plan-banner" [attr.aria-label]="t('shell.planBanner.ariaLabel')" *transloco="let t">
      <app-icon name="sparkles" />
      <p>{{ t('shell.planBanner.notice') }}</p>
      <button class="upgrade" type="button">
        {{ t('shell.planBanner.upgrade') }} <app-icon name="arrowUpRight" />
      </button>
      <button
        class="dismiss"
        type="button"
        [attr.aria-label]="t('shell.planBanner.dismiss')"
        (click)="shell.bannerVisible.set(false)"
      >
        <app-icon name="x" />
      </button>
    </aside>
  `,
  styleUrl: './plan-banner.css',
})
export class PlanBanner {
  protected readonly shell = inject(ConsoleShellFacade);
}
