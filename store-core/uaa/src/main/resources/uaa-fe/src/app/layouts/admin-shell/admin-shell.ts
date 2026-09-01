import {Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {AuthService} from '@cvhome-saas/ui-kit';
import {Icon, type IconName} from '@cvhome-saas/ui-kit/ui';

interface NavItem {
  readonly path: string;
  readonly icon: IconName;
  readonly labelKey: string;
}

/**
 * The chrome the three admin sections sit inside.
 *
 * Deliberately thin next to console-ui's shell: there is no store switcher, no breadcrumb trail and
 * no locale persistence, because uaa is platform-wide and this app administers exactly three things.
 * Anything richer than this belongs in the console, which is where an operator spends their day.
 */
@Component({
  selector: 'app-admin-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslocoDirective, Icon],
  templateUrl: './admin-shell.html',
  styleUrl: './admin-shell.css',
})
export class AdminShell {
  private readonly auth = inject(AuthService);

  protected readonly nav: readonly NavItem[] = [
    {path: '/users', icon: 'users', labelKey: 'nav.users'},
    {path: '/roles', icon: 'shield', labelKey: 'nav.roles'},
    {path: '/clients', icon: 'code', labelKey: 'nav.clients'},
  ];

  /** The signed-in operator, or `null` before `canAccessSecuredPages` has resolved one. */
  protected readonly user = this.auth.getCachedAuthUser() ?? null;

  protected logout(): void {
    this.auth.logout();
  }
}
