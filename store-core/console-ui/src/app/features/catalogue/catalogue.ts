import {Component, computed, effect, inject, input} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {isCatalogueTab} from '@models/taxonomy';
import {BrandTab} from './components/brand-tab/brand-tab';
import {CategoryTab} from './components/category-tab/category-tab';
import {GroupTab} from './components/group-tab/group-tab';
import {TypeTab} from './components/type-tab/type-tab';
import {CatalogueFacade} from './facades/catalogue.facade';

/**
 * The catalogue: categories, product types, brands and product groups.
 *
 * Renders into `ConsoleShell`, so this is only its own content — a page header, a tab strip and
 * whichever tab is open.
 *
 * **The tab is in the URL.** `withComponentInputBinding()` binds `:tab` onto `tab` below, which is
 * what makes a tab linkable and makes the browser's back button walk between them — the shape
 * store management and billing already use for their sections.
 *
 * **`/products` is a separate page, not a fifth tab.** These four are taxonomy: small records, a
 * list and an editor side by side, no paging. The product list is a paged, filtered table with its
 * own wizard behind it, and folding it in here would give one route two entirely different
 * behaviours.
 */
@Component({
  providers: [CatalogueFacade],
  selector: 'app-catalogue',
  imports: [
    LoadError,
    BrandTab,
    BusyOverlay,
    CategoryTab,
    ConfirmDialog,
    GroupTab,
    Icon,
    PageHeader,
    TabSwitcher,
    TranslocoDirective,
    TypeTab,
  ],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class Catalogue {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);

  protected readonly facade = inject(CatalogueFacade);

  /** The `:tab` route param. Validated before it reaches the facade — `/catalogue/nonsense` is a 404. */
  readonly tab = input<string>();

  protected readonly heading = this.facade.heading;
  protected readonly tabs = this.facade.tabs;
  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;

  constructor() {
    /*
     * The URL is the source of truth for which tab is open, so this runs one way: route → facade.
     * `activeTab` is written back to the URL by `onTabChange` rather than by an effect, which is
     * what keeps a tab click from producing two navigations.
     */
    effect(() => {
      const requested = this.tab();
      if (isCatalogueTab(requested)) {
        this.facade.activeTab.set(requested);
      } else if (requested !== undefined) {
        this.router.navigate(['/catalogue', 'categories'], {replaceUrl: true});
      }
    });
  }

  protected readonly activeTab = computed(() => this.facade.activeTab());

  /** The context line under the title, once a response says how big the catalogue is. */
  protected readonly context = computed(() => {
    this.transloco.activeLang();
    if (this.isEmpty()) {
      return this.heading().context;
    }
    return this.transloco.translate('catalogue.heading.contextWithCounts', {
      store: this.heading().context,
      categories: this.facade.categoryCount(),
      brands: this.facade.brands().length,
    });
  });

  protected onTabChange(key: string): void {
    if (isCatalogueTab(key) && key !== this.facade.activeTab()) {
      this.router.navigate(['/catalogue', key]);
    }
  }

  protected onAdd(): void {
    this.facade.startCreate();
  }

  protected deleteTitle(): string {
    const pending = this.facade.pendingDelete();
    return pending
      ? this.transloco.translate(`catalogue.delete.${pending.tab}`, {name: pending.name})
      : '';
  }

  protected deleteMessage(): string {
    const pending = this.facade.pendingDelete();
    return pending ? this.transloco.translate(`catalogue.deleteMessage.${pending.tab}`) : '';
  }
}
