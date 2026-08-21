import {Component, computed, inject} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {ConsoleSidebar} from './components/console-sidebar/console-sidebar';
import {ConsoleToolbar} from './components/console-toolbar/console-toolbar';
import {PlanBanner} from './components/plan-banner/plan-banner';
import {BillingFacade} from '@layouts/billing/billing.facade';
import {ConsoleShellFacade} from './facades/console-shell.facade';

/**
 * The authenticated console frame: plan banner, navigation rail, toolbar, and the outlet
 * every console page renders into.
 *
 * Pages own their content and nothing else. To add one, register it as a child of this
 * shell's route with a `breadcrumbKey` in its route data — the toolbar reads that, so the
 * shell needs no knowledge of the page.
 */
@Component({
  selector: 'app-console-shell',
  imports: [ConsoleSidebar, ConsoleToolbar, Icon, PlanBanner, RouterOutlet, TranslocoDirective],
  template: `
    <div class="console" [class.banner-on]="bannerShown()" *transloco="let t">
      @if (bannerShown()) {
        <app-plan-banner />
      }

      <div class="console-body" [class.nav-collapsed]="shell.navCollapsed()">
        <button
          class="mobile-nav-toggle"
          type="button"
          [attr.aria-label]="t('shell.nav.openNavigation')"
          [attr.aria-expanded]="shell.mobileNavOpen()"
          (click)="$event.stopPropagation(); shell.mobileNavOpen.set(true)"
        >
          <app-icon name="menu" />
        </button>

        @if (shell.mobileNavOpen()) {
          <button
            class="nav-scrim"
            type="button"
            [attr.aria-label]="t('shell.nav.closeNavigation')"
            (click)="shell.mobileNavOpen.set(false)"
          ></button>
        }

        <app-console-sidebar />

        <main class="workspace">
          <app-console-toolbar />
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styleUrl: './console-shell.css',
  host: {
    '(document:click)': 'shell.closeMenus($event)',
    '(document:keydown.escape)': 'dismiss()',
  },
})
export class ConsoleShell {
  protected readonly shell = inject(ConsoleShellFacade);
  private readonly billing = inject(BillingFacade);

  /**
   * Whether the banner occupies a row.
   *
   * Asked here rather than on `ConsoleShellFacade` because the answer now depends on billing, and
   * `BillingFacade` already injects the shell facade — putting it there would close a dependency
   * cycle. The layout needs the same answer the banner does: `.banner-on` reserves the row's height,
   * so gating only the component would leave a 49px gap above every page whenever there is nothing
   * to say, which is the normal case for a paying customer.
   */
  protected readonly bannerShown = computed(
    () => this.shell.bannerShown() && this.billing.banner() !== null,
  );

  /** Escape backs out of whatever is open — a menu, or the mobile drawer. */
  protected dismiss(): void {
    if (this.shell.openMenu()) {
      this.shell.closeMenus();
      return;
    }
    this.shell.closeMobileNav();
  }
}
