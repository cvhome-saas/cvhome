import {Component, computed, inject, signal} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NavigationEnd, Router, RouterLink, RouterOutlet} from '@angular/router';
import {filter, map, startWith} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {AuthService} from '@cvhome-saas/ui-kit';
import {LocaleService, type LocaleCode} from '@cvhome-saas/ui-kit/i18n';
import {Icon, SectionNav, type NavSection} from '@cvhome-saas/ui-kit/ui';

import {isRealmAdmin} from '@shared/auth/realm-admin';

/**
 * The chrome the three admin sections sit inside: a grouped rail beside a topbar and the page.
 *
 * **The rail is `app-section-nav`, not a bespoke component.** It was already a router-bound vertical
 * rail that collapses to an icon strip; it gained grouping and a disabled row for this design, which
 * console-ui's own sidebar could adopt later. Writing a second grouped rail here is exactly the
 * duplication the kit exists to prevent.
 *
 * **Every section in this rail is a real route.** Dashboard, Audit log and Identity providers were drawn
 * disabled here because nothing stood behind them; feat/uaa-sso built all three, so the rail is now a map of
 * the product with no dead rows. The count badge and the realm switcher the design also draws are still
 * absent — see lessons.md, "Shell — four sections have no backend" and "Shell — no sidebar badge counts".
 */
@Component({
  selector: 'app-admin-shell',
  imports: [RouterOutlet,
    RouterLink, TranslocoDirective, Icon, SectionNav],
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
  private readonly adminSections: readonly NavSection[] = [
    {key: 'dashboard', labelKey: 'nav.dashboard', icon: 'home', group: 'nav.group.overview'},
    {key: 'audit', labelKey: 'nav.audit', icon: 'clock', group: 'nav.group.overview'},
    {key: 'users', labelKey: 'nav.users', icon: 'users', group: 'nav.group.identity'},
    {key: 'roles', labelKey: 'nav.roles', icon: 'shield', group: 'nav.group.identity'},
    {key: 'identity-providers', labelKey: 'nav.providers', icon: 'signIn', group: 'nav.group.identity'},
    {key: 'clients', labelKey: 'nav.clients', icon: 'layoutGrid', group: 'nav.group.applications'},
    {key: 'settings', labelKey: 'nav.settings', icon: 'cog', group: 'nav.group.system'},
  ];

  /**
   * What this person can actually open.
   *
   * Every screen above is behind `SCOPE_super_admin`/`ROLE_SUPER_ADMIN` on the server, so for anyone
   * else the whole rail is a list of doors that answer 403. An org administrator used to sign in,
   * land on an admin screen and read an access-denied bar with the full navigation still beside it —
   * a rail that offers what the API refuses is worse than no rail. They get their own account
   * instead, which is the one thing this console can show them.
   */
  protected readonly sections = computed<readonly NavSection[]>(() =>
    isRealmAdmin(this.auth)
      ? this.adminSections
      : [{key: 'account', labelKey: 'nav.account', icon: 'user', group: 'nav.group.you'}],
  );

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

  /**
   * The breadcrumb's trailing label — the section the operator is in, or the account page, which has no rail
   * row (it is reached from the who-chip) and so is named explicitly rather than falling back to Users.
   */
  protected readonly currentLabelKey = computed(() => {
    const active = this.active();
    if (active === 'account') {
      return 'nav.account';
    }
    return this.sections().find((s) => s.key === active)?.labelKey ?? 'nav.account';
  });

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
