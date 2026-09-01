import {Injectable, computed, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {AuthService} from '@cvhome-saas/ui-kit';
import {LocaleService, type ConsoleLocale, type LocaleCode} from '@core/i18n/locale.service';
import {ThemeService, type ThemeOption} from '@core/theme/theme.service';
import {RoleLabel} from '@shared/i18n/role-label';

/** The `ROLE_` prefix Spring puts on an authority, which is not part of the role's name. */
const ROLE_PREFIX = 'ROLE_';

/**
 * The signed-in operator's own account.
 *
 * **The only facade in the console with no api service and no HTTP call**, and that is the finding
 * rather than an oversight. Everything the design's Profile tab asks for is either not stored or not
 * readable:
 *
 * - The account record is unreachable. `GET …/user-account/current` 500s, and fixing its
 *   `Principal`/`Jwt` binding would turn that into a 404, because the id it would then pass is the
 *   **username** while the lookup behind it is uaa's by-UUID one. `find-one?userId={sub}` fails the
 *   same way, and for an org admin the store guard would refuse it even if it did not.
 * - The token carries no name and no email either: the gateway's OAuth client requests only the
 *   `openid` scope, so the ID token has no `given_name`, `family_name` or `email` to carry.
 * - Job title, phone, timezone, date format, bio and an avatar have no column anywhere.
 * - Notification preferences, two-factor and a session list have no service at all.
 *
 * What is left is genuinely true and genuinely useful: the username, the roles, and the two
 * preferences the console owns outright. Both of those are client-side and cost no endpoint — which
 * is also why they cannot follow the operator to another machine, and the page says so.
 *
 * See lessons.md, "Users — the JWT carries no user id", "Users — the ID token requests only the
 * `openid` scope" and "Shell — no user-preferences endpoint".
 */
@Injectable()
export class ProfileFacade {
  private readonly auth = inject(AuthService);
  private readonly transloco = inject(TranslocoService);
  private readonly roleLabels = inject(RoleLabel);
  private readonly locales = inject(LocaleService);
  private readonly theme = inject(ThemeService);

  /** uaa's username. The only human-readable identity the platform will give the console. */
  readonly userName = computed(() => this.auth.getCachedAuthUser()?.username ?? '');

  /**
   * The roles the token carries, stripped of Spring's `ROLE_` prefix.
   *
   * Read from the access token's `roles` claim, which `JwtCustomizerConfig` does populate — so
   * unlike the name and the email, this is real. Scope authorities (`SCOPE_*`) are dropped: they
   * describe the client, not the person.
   */
  readonly roles = computed<readonly string[]>(() => {
    const authorities = this.auth.getCachedAuthUser()?.authorities ?? [];
    return authorities
      .filter((authority) => authority.startsWith(ROLE_PREFIX))
      .map((authority) => authority.slice(ROLE_PREFIX.length));
  });

  /** Every role, joined — for the line under the monogram. */
  readonly roleLabel = computed(() => this.roleLabels.labels(this.roles()));

  /**
   * One role, through the known-set guard.
   *
   * uaa's roles are a **table**, not an enum, so a row added with `POST /api/v1/admin/roles` would
   * arrive here and Transloco throws on a missing key. A blind `role.{{value}}` lookup would take
   * the page down; this humanizes instead.
   */
  labelFor(role: string): string {
    return this.roleLabels.label(role);
  }

  /** A monogram. From the username, because there is no name to take one from. */
  readonly initials = computed(() =>
    this.userName()
      .split(/[\s._-]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join(''),
  );

  readonly localeOptions = this.locales.locales;
  readonly activeLocale = computed<LocaleCode>(() => this.locales.currentLocale().code);

  readonly themeOptions = this.theme.themes;
  readonly activeTheme = this.theme.current;

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('profile.heading.title'),
      context: this.transloco.translate('profile.heading.context'),
    };
  });

  selectLocale(code: LocaleCode): void {
    this.locales.select(code);
  }

  selectTheme(id: string): void {
    this.theme.select(id);
  }

  themeLabel(option: ThemeOption): string {
    this.transloco.activeLang();
    return this.transloco.translate(option.labelKey);
  }

  themeHint(option: ThemeOption): string {
    this.transloco.activeLang();
    return this.transloco.translate(option.hintKey);
  }

  localeLabel(locale: ConsoleLocale): string {
    return locale.label;
  }
}
