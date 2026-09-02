import {Component, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {filter, map, startWith} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {AuthService} from '@cvhome-saas/ui-kit';
import {LocaleService, type LocaleCode} from '@cvhome-saas/ui-kit/i18n';
import {Icon, SectionNav, type NavSection} from '@cvhome-saas/ui-kit/ui';

/**
 * The chrome the three admin sections sit inside: a grouped rail beside a topbar and the page.
 *
 * **The rail is `app-section-nav`, not a bespoke component.** It was already a router-bound vertical
 * rail that collapses to an icon strip; it gained grouping and a disabled row for this design, which
 * console-ui's own sidebar could adopt later. Writing a second grouped rail here is exactly the
 * duplication the kit exists to prevent.
 *
 * **Three sections are rendered disabled.** Dashboard, Audit log and Identity providers are
 * in the design and have no backend at all — no endpoint, no data, nothing behind them. They are
 * shown rather than hidden so the rail is a map of the product rather than of this sprint, and each
 * says why on hover. See lessons.md, "Shell — four sections have no backend"; the count badge and
 * realm switcher the design also draws are in there too.
 */
@Component({
  selector: 'app-admin-shell',
  imports: [RouterOutlet, TranslocoDirective, Icon, SectionNav],
  templateUrl: './admin-shell.html',
  styleUrl: './admin-shell.css',
})
export class AdminShell {
  private readonly auth = inject(AuthService);
  private readonly locale = inject(LocaleService);

  /**
   * The rail, in the design's order.
   *
   * `key` is the route segment and the active handle; `basePath` is `/`, so `users` is `/users`.
   * The unbuilt four carry no count and no attention dot — an invented number in a navigation rail
   * is read as fact, which is the finding console-ui already recorded against this same design
   * (lessons.md, "Shell — no sidebar badge counts").
   */
  protected readonly sections: readonly NavSection[] = [
    {key: 'dashboard', labelKey: 'nav.dashboard', icon: 'home', group: 'nav.group.overview',
     disabled: true, disabledHintKey: 'nav.notBuilt'},
    {key: 'audit', labelKey: 'nav.audit', icon: 'clock', group: 'nav.group.overview',
     disabled: true, disabledHintKey: 'nav.notBuilt'},
    {key: 'users', labelKey: 'nav.users', icon: 'users', group: 'nav.group.identity'},
    {key: 'roles', labelKey: 'nav.roles', icon: 'shield', group: 'nav.group.identity'},
    {key: 'providers', labelKey: 'nav.providers', icon: 'signIn', group: 'nav.group.identity',
     disabled: true, disabledHintKey: 'nav.notBuilt'},
    {key: 'clients', labelKey: 'nav.clients', icon: 'layoutGrid', group: 'nav.group.applications'},
    {key: 'settings', labelKey: 'nav.settings', icon: 'cog', group: 'nav.group.system'},
  ];

  protected readonly collapsed = signal(false);
  protected readonly mobileNavOpen = signal(false);
  protected readonly menuOpen = signal(false);

  protected readonly locales = this.locale.locales;
  protected readonly activeLocale = this.locale.current;

  /** The signed-in operator, or `null` before `canAccessSecuredPages` has resolved one. */
  protected readonly user = this.auth.getCachedAuthUser() ?? null;

  private readonly router = inject(Router);

  /**
   * The rail's active handle: the first path segment, which is what a section `key` is.
   *
   * Read from the router rather than from `location`, so it follows an in-app navigation as well as
   * a full load — and closes the mobile drawer on the way.
   */
  protected readonly active = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => firstSegment(event.urlAfterRedirects)),
      startWith(firstSegment(this.router.url)),
    ),
    {initialValue: firstSegment(this.router.url)},
  );

  /** The breadcrumb's trailing label — the section the operator is in. */
  protected readonly currentLabelKey = computed(
    () => this.sections.find((s) => s.key === this.active())?.labelKey ?? 'nav.users',
  );

  protected selectLocale(code: LocaleCode): void {
    this.locale.select(code);
  }

  protected logout(): void {
    this.auth.logout();
  }
}

/** `/clients?x=1` -> `clients`. The rail keys are first segments. */
function firstSegment(url: string): string {
  return url.split(/[?#]/)[0].split('/').filter(Boolean)[0] ?? 'users';
}
