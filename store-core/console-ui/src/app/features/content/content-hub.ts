import {Component, computed, effect, inject, input, signal} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {CONTENT_TABS, type ContentListType, type ContentRow, type ContentTab} from '@models/content';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import {Icon} from '@shared/ui/icon/icon';
import {KpiGrid} from '@shared/ui/kpi-grid/kpi-grid';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {SectionNav} from '@shared/ui/section-nav/section-nav';
import {TabSwitcher, type TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import {ContentList} from './components/content-list/content-list';
import {MediaTab} from './components/media-tab/media-tab';
import {ContentHubFacade} from './facades/content-hub.facade';

const LIST_TABS: readonly ContentListType[] = ['pages', 'posts', 'banners', 'faq', 'policies'];

/**
 * Content management — `Content Management.dc.html`: the KPI strip, the seven-tab rail with counts,
 * and the tab's panel. The tab is a URL segment (`/content/:tab`), so it is linkable and survives a
 * reload; an unknown one falls back to `pages` here rather than 404ing.
 *
 * The "Home page builder" link the design shows is not offered: the builder is a later module, and
 * the store-management rail already carries the disabled entry with the reason.
 */
@Component({
  selector: 'app-content-hub',
  imports: [ContentList, Icon, KpiGrid, LoadError, MediaTab, PageHeader, SectionNav, TabSwitcher, TranslocoDirective],
  templateUrl: './content-hub.html',
  styleUrl: './content-hub.css',
})
export class ContentHub {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);
  private readonly permissions = inject(ConsolePermissions);
  protected readonly facade = inject(ContentHubFacade);

  /** Bound from the route by `withComponentInputBinding()`. */
  readonly tab = input<string>('pages');

  protected readonly railCollapsed = signal(false);
  protected readonly canManage = computed(() => this.permissions.canManageContent());

  protected readonly activeTab = computed<ContentTab>(() => {
    const tab = this.tab();
    return (CONTENT_TABS as readonly string[]).includes(tab) ? (tab as ContentTab) : 'pages';
  });

  protected readonly listType = computed<ContentListType | null>(() => {
    const tab = this.activeTab();
    return (LIST_TABS as readonly string[]).includes(tab) ? (tab as ContentListType) : null;
  });

  constructor() {
    effect(() => {
      this.facade.activeTab.set(this.activeTab());
      if (this.tab() !== this.activeTab()) {
        this.router.navigate(['/content', this.activeTab()], {replaceUrl: true});
      }
    });
  }

  protected readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return this.facade.sections().map((section) => ({
      key: section.key,
      label: this.transloco.translate(section.labelKey),
      badge: section.count ?? undefined,
    }));
  });

  /** The header's primary action follows the tab: "New page", "Upload files", … */
  protected readonly createLabel = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate(`content.create.${this.activeTab()}`);
  });

  protected readonly createAvailable = computed(() => this.listType() !== null && this.canManage());

  protected pickTab(key: string): void {
    this.router.navigate(['/content', key]);
  }

  protected create(): void {
    const type = this.listType();
    if (type) {
      this.router.navigate(['/content', type, 'new']);
    }
  }

  protected openRow(row: ContentRow): void {
    const type = this.listType();
    if (type) {
      this.router.navigate(['/content', type, row.id]);
    }
  }
}
