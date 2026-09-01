import {Component, inject} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {AuthService} from '@cvhome-saas/ui-kit';
import {LocaleService, type LocaleCode} from '@cvhome-saas/ui-kit/i18n';
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
  private readonly locale = inject(LocaleService);

  /*
   * A plain toggle rather than the kit's `app-locale-switcher`: that control answers "which locales
   * has this piece of content been written in", and takes a `filled` set to say so. This is the UI
   * language, where both are always available. `LocaleService` is what puts `dir="rtl"` on the
   * document, so Arabic mirrors the whole app rather than only translating it.
   */
  protected readonly locales = this.locale.locales;
  protected readonly activeLocale = this.locale.current;

  protected readonly nav: readonly NavItem[] = [
    {path: '/users', icon: 'users', labelKey: 'nav.users'},
    {path: '/roles', icon: 'shield', labelKey: 'nav.roles'},
    {path: '/clients', icon: 'code', labelKey: 'nav.clients'},
  ];

  /** The signed-in operator, or `null` before `canAccessSecuredPages` has resolved one. */
  protected readonly user = this.auth.getCachedAuthUser() ?? null;

  protected selectLocale(code: LocaleCode): void {
    this.locale.select(code);
  }

  protected logout(): void {
    this.auth.logout();
  }
}
