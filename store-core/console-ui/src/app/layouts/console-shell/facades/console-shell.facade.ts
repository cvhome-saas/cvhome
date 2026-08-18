import {DOCUMENT} from '@angular/common';
import {computed, DestroyRef, effect, inject, Injectable, linkedSignal, signal} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {translateSignal} from '@jsverse/transloco';
import {filter, map, startWith} from 'rxjs';

import {CONSOLE_LOCALES, LocaleCode, LocaleService} from '@core/i18n/locale.service';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {THEME} from '@core/theme/theme.provider';
import type {ConsoleNotification, ConsoleStore} from '@models/console';
import {ConsoleApi} from '../services/console.api.service';

/** Which of the shell's popovers is open. Only one at a time. */
export type ConsoleMenu = 'notifications' | 'language' | 'theme' | 'profile' | 'store';

export interface ConsoleLanguageOption {
  readonly code: LocaleCode;
  readonly name: string;
}

/**
 * State and data for the console chrome, shared by every page rendered inside the shell.
 *
 * Pages never touch this — they render into the shell's outlet and own only their own
 * content.
 */
@Injectable({providedIn: 'root'})
export class ConsoleShellFacade {
  private readonly api = inject(ConsoleApi);
  private readonly theme = inject(THEME);
  private readonly destroyRef = inject(DestroyRef);
  private readonly window = inject(DOCUMENT).defaultView;
  private readonly locale = inject(LocaleService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly selection = inject(SelectedStoreService);

  readonly navigation = this.api.navigation;

  /**
   * The signed-in operator. Null until the request answers, which the toolbar renders as a neutral
   * placeholder rather than inventing a name.
   */
  private readonly identity = rxResource({stream: () => this.api.loadUser()});
  readonly user = this.identity.value;

  /**
   * TODO(lessons.md): the organization's name — no endpoint. See lessons.md, "Shell — an org admin
   * cannot read its own organization".
   *
   * `OrgManagerApi` is super-admin only on every method, so the console holds an org *id* from the
   * principal and has no way to resolve a name for it. The toolbar shows nothing here rather than the
   * fixture's "ACME".
   */
  readonly organization: string | null = null;

  /**
   * TODO(lessons.md): notifications — no service exists. See lessons.md, "Shell — no notifications
   * service". The bell is rendered disabled rather than opening an empty popover that looks broken.
   */
  readonly notifications: readonly ConsoleNotification[] = [];

  readonly bannerVisible = signal(true);
  readonly navCollapsed = signal(false);
  readonly mobileNavOpen = signal(false);
  readonly openMenu = signal<ConsoleMenu | null>(null);

  /**
   * The store directory, fetched rather than held as a constant: the rail is the one part of
   * the chrome whose contents belong to the account, not to the build.
   */
  private readonly directory = rxResource({stream: () => this.api.loadStores()});

  /**
   * The rail's stores, in the order tenancy returned them.
   *
   * There is no client-side ordering: a rail order the operator arranges would have to live somewhere,
   * and nothing on the backend can hold it — see lessons.md, "Shell — no user-preferences endpoint".
   */
  readonly stores = computed<readonly ConsoleStore[]>(() => this.directory.value()?.stores ?? []);
  readonly storesLoading = this.directory.isLoading;

  /** Which store the console is working in. */
  readonly currentStoreId = linkedSignal(() => this.directory.value()?.currentStoreId ?? null);

  /**
   * True when the account owns no store yet.
   *
   * Everything in the console is a reading of one store, so until one exists the rail
   * leads nowhere and the guards hold the operator on the getting-started page. Gated on
   * `storesLoading` because an empty list mid-request is absence of an answer, not an
   * answer — without it the rail would flash disabled on every load.
   */
  readonly firstRun = computed(() => !this.storesLoading() && this.stores().length === 0);

  /**
   * Whether the plan strip occupies the top of the shell.
   *
   * Suppressed during first run: the getting-started page runs its own trial notice in
   * that slot, and two stacked strips saying overlapping things about the plan is one
   * more than the operator needs. One signal rather than two conditions in the template,
   * because the strip's visibility and the `--banner-h` it reserves must never disagree.
   */
  readonly bannerShown = computed(() => this.bannerVisible() && !this.firstRun());

  readonly currentStore = computed(
    () => this.stores().find((store) => store.id === this.currentStoreId()) ?? null,
  );

  readonly unreadCount = computed(
    () => this.notifications.filter((notification) => notification.unread).length,
  );

  /**
   * True while the rail is an off-canvas drawer rather than a persistent column.
   *
   * The breakpoint comes from the theme so this cannot drift from the media queries in
   * the shell's stylesheets, which use the same token's value.
   */
  readonly isCompact = signal(false);

  constructor() {
    const query = this.window?.matchMedia?.(`(max-width: ${this.theme('--breakpoint-lg')})`);
    if (query) {
      this.isCompact.set(query.matches);
      const onChange = (event: MediaQueryListEvent) => this.isCompact.set(event.matches);
      query.addEventListener('change', onChange);
      this.destroyRef.onDestroy(() => query.removeEventListener('change', onChange));
    }

    // Leaving compact turns the drawer back into a static column; drop the open flag so
    // it does not reappear the next time the viewport narrows.
    effect(() => {
      if (!this.isCompact()) {
        this.mobileNavOpen.set(false);
      }
    });
  }

  /** Dismisses the drawer, and any open menu with it. */
  closeMobileNav(): void {
    this.mobileNavOpen.set(false);
    this.openMenu.set(null);
  }

  /**
   * The active route's breadcrumb key, taken from its `breadcrumbKey` data. Keeping it on
   * the route means a new page announces itself without the shell needing to know
   * anything about it.
   */
  private readonly breadcrumbKey = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      startWith(null),
      map(() => (this.deepestRouteData()['breadcrumbKey'] as string | undefined) ?? ''),
    ),
    {initialValue: (this.deepestRouteData()['breadcrumbKey'] as string | undefined) ?? ''},
  );

  /** The breadcrumb's last crumb, translated and reactive to language changes. */
  readonly pageLabel = translateSignal(this.breadcrumbKey);

  readonly language = computed<ConsoleLanguageOption>(() => {
    const current = this.locale.currentLocale();
    return {code: current.code, name: current.label};
  });

  readonly languages: readonly ConsoleLanguageOption[] = CONSOLE_LOCALES.map((option) => ({
    code: option.code,
    name: option.label,
  }));

  selectLanguage(code: LocaleCode): void {
    this.locale.select(code);
    this.closeMenus();
  }

  toggleMenu(menu: ConsoleMenu): void {
    this.openMenu.update((open) => (open === menu ? null : menu));
  }

  closeMenus(event?: Event): void {
    // A click inside a menu is the menu's own business — only outside clicks dismiss.
    if (event?.type === 'click' && event.target instanceof Element && event.target.closest('.menu')) {
      return;
    }
    this.openMenu.set(null);
  }

  /** Opens a store. The selection is what the API layer stamps on every later request. */
  selectStore(storeId: string): void {
    if (this.currentStoreId() === storeId) {
      return;
    }
    this.currentStoreId.set(storeId);
    this.selection.selectStore(storeId);
    this.closeMobileNav();
  }

  /** Re-reads the directory. Called once a store has been created, so the rail and the guards agree. */
  refreshStores(): void {
    this.directory.reload();
  }

  private deepestRouteData(): Record<string, unknown> {
    let route = this.route.snapshot;
    while (route.firstChild) {
      route = route.firstChild;
    }
    return route.data;
  }
}
