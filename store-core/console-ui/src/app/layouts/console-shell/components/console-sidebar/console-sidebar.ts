import {NgTemplateOutlet} from '@angular/common';
import {Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {TranslocoDirective, TranslocoPipe} from '@jsverse/transloco';

import {Icon, IconName} from '@cvhome-saas/ui-kit/ui';
import {ConsoleShellFacade} from '../../facades/console-shell.facade';
import {StoreSwitcher} from '../store-switcher/store-switcher';

/**
 * Organisation header, primary navigation, and the store switcher.
 *
 * Which item is highlighted comes from the router rather than the navigation data, so a
 * page cannot be added without the rail following it there.
 */
@Component({
  selector: 'app-console-sidebar',
  imports: [Icon, NgTemplateOutlet, RouterLink, RouterLinkActive, StoreSwitcher, TranslocoDirective, TranslocoPipe],
  template: `
    <aside class="sidebar" [class.mobile-open]="shell.mobileNavOpen()" *transloco="let t">
      <header class="sidebar-head">
        <!--
          TODO(lessons.md): the organization's name. OrgManagerApi is super-admin only on every
          method, so an org admin cannot read its own org — see lessons.md, "Shell — an org admin
          cannot read its own organization". The brand stands in until there is a name to show.
        -->
        <span class="org-mark" aria-hidden="true">{{ brandName.charAt(0) }}</span>
        <strong class="nav-label">{{ brandName }}</strong>
        <button
          class="nav-toggle"
          type="button"
          [attr.title]="toggleLabel(t)"
          [attr.aria-label]="toggleLabel(t)"
          (click)="toggleNav()"
        >
          <app-icon [name]="toggleIcon()" />
        </button>
      </header>

      <nav
        class="sidebar-nav"
        [class.first-run]="shell.firstRun()"
        [attr.aria-label]="t('shell.nav.ariaLabel')"
      >
        @for (section of shell.navigation; track section.groupKey) {
          <p class="nav-group nav-label">{{ t(section.groupKey) }}</p>
          @for (item of section.items; track item.labelKey) {
            <!--
              Every section is a reading of one store, so until one exists they all lead
              nowhere — which is exactly what a routeless item already means here. Reusing
              that branch keeps one definition of "disabled" in the rail.
            -->
            @if (item.route && !shell.firstRun()) {
              <a
                class="nav-item"
                routerLinkActive="active"
                #link="routerLinkActive"
                [routerLink]="item.route"
                [attr.aria-current]="link.isActive ? 'page' : null"
                [attr.title]="shell.navCollapsed() ? t(item.labelKey) : null"
                (click)="shell.mobileNavOpen.set(false)"
              >
                <ng-container *ngTemplateOutlet="navBody; context: {$implicit: item}" />
              </a>
            } @else {
              <button
                class="nav-item"
                type="button"
                [disabled]="shell.firstRun()"
                [attr.aria-disabled]="shell.firstRun() ? 'true' : null"
                [attr.title]="disabledReason(t) ?? (shell.navCollapsed() ? t(item.labelKey) : null)"
                (click)="shell.mobileNavOpen.set(false)"
              >
                <ng-container *ngTemplateOutlet="navBody; context: {$implicit: item}" />
              </button>
            }
          }
        }
      </nav>

      @if (shell.storeSwitcherShown()) {
        <app-store-switcher />
      }
    </aside>

    <ng-template #navBody let-item>
      <app-icon [name]="item.icon" />
      <span class="nav-label">{{ item.labelKey | transloco }}</span>
      @if (item.badge) {
        <b class="nav-label badge" [class]="item.badgeTone">{{ item.badge }}</b>
      }
    </ng-template>
  `,
  styleUrl: './console-sidebar.css',
})
export class ConsoleSidebar {
  protected readonly shell = inject(ConsoleShellFacade);

  // Brand name, not translated — same convention as the marketing page.
  protected readonly brandName = 'cvhome';

  /**
   * Collapsing the rail is meaningless while it is a drawer — the mobile layout pins it to
   * full width — so at that size the same control dismisses the drawer instead. Otherwise
   * the button silently toggled a state with no visible effect, and left the rail
   * collapsed for the next time the viewport widened.
   */
  protected toggleNav(): void {
    if (this.shell.isCompact()) {
      this.shell.closeMobileNav();
      return;
    }
    this.shell.navCollapsed.set(!this.shell.navCollapsed());
  }

  /**
   * Why the rail is inert, for the item's tooltip. A disabled control that says nothing is
   * just a broken one — the same reasoning as the settings rail's unavailable builder.
   */
  protected disabledReason(t: (key: string) => string): string | null {
    return this.shell.firstRun() ? t('shell.nav.firstRunDisabled') : null;
  }

  protected toggleIcon(): IconName {
    if (this.shell.isCompact()) {
      return 'x';
    }
    return this.shell.navCollapsed() ? 'panelLeftOpen' : 'panelLeftClose';
  }

  protected toggleLabel(t: (key: string) => string): string {
    if (this.shell.isCompact()) {
      return t('shell.nav.closeNavigation');
    }
    return this.shell.navCollapsed() ? t('shell.nav.expandMenu') : t('shell.nav.collapseMenu');
  }
}
