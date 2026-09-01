import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';
import {catchError, map, of} from 'rxjs';

import {ContentCache} from '@api/content/content-cache';
import {ContentSummaryService} from '@api/content/content-summary.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {CONTENT_TABS, type ContentSummary, type ContentTab} from '@models/content';
import type {KpiDatum} from '@cvhome-saas/ui-kit';
import type {NavSection} from '@shared/ui/section-nav/section-nav';
import type {IconName} from '@shared/ui/icon/icon-paths';

const TAB_ICONS: Readonly<Record<ContentTab, IconName>> = {
  pages: 'file',
  posts: 'messageCircle',
  banners: 'images',
  faq: 'questionCircle',
  media: 'layoutGrid',
  menus: 'sitemap',
  policies: 'shield',
  branding: 'palette',
};

/** The summary's `counts` key for each tab. */
const COUNT_KEYS: Readonly<Record<ContentTab, string>> = {
  pages: 'pages',
  posts: 'posts',
  banners: 'banners',
  faq: 'faq',
  media: 'media',
  menus: 'menus',
  policies: 'policies',
  // One record per store, so there is nothing to count; the rail shows no badge for it.
  branding: 'branding',
};

/**
 * The store's storefront languages, as every editor and list needs them.
 *
 * Read from the merchant store record — the console's own en/ar are the languages the *console*
 * runs in, not the ones the storefront publishes in, and an editor has to offer the latter.
 */
export interface StoreLocales {
  readonly codes: readonly string[];
  readonly defaultCode: string;
}

/**
 * What the Content management hub needs once: the summary for the KPI strip and the rail counts, the
 * store's languages for everything below, and which tab is open.
 *
 * Root-provided on purpose: the editors are sibling routes of the hub, not children rendered in its
 * outlet, so a page-provided instance could not reach them, and providing it on the `content` route
 * branch would statically import this facade — and the api tier behind it — into the main bundle.
 */
@Injectable({providedIn: 'root'})
export class ContentHubFacade {
  private readonly summaryApi = inject(ContentSummaryService);
  private readonly storeApi = inject(MerchantStoreService);
  private readonly reference = inject(ReferenceDataService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

  readonly activeTab = signal<ContentTab>('pages');

  private readonly summaryResource = rxResource({
    params: () => {
      this.cache.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () => this.summaryApi.summary(),
  });

  private readonly summary = linkedSignal<ContentSummary | undefined, ContentSummary | undefined>({
    source: () => (this.summaryResource.hasValue() ? this.summaryResource.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly summaryLoading = this.summaryResource.isLoading;
  readonly summaryError = computed(() => this.summaryResource.error() as Error | undefined);

  /**
   * The store's languages. Falls back to English when the store record cannot be read, so an
   * editor always has at least one locale to write in rather than none.
   */
  private readonly localesResource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () =>
      this.storeApi.store().pipe(
        map((store): StoreLocales => {
          const codes = [
            ...new Set([...(store.supportedLanguages ?? []), store.defaultLanguage ?? 'en']),
          ];
          return {codes, defaultCode: store.defaultLanguage ?? codes[0] ?? 'en'};
        }),
        catchError(() => of<StoreLocales>({codes: ['en'], defaultCode: 'en'})),
      ),
  });

  readonly locales = computed<StoreLocales>(() =>
    this.localesResource.hasValue()
      ? this.localesResource.value()
      : {codes: ['en'], defaultCode: 'en'},
  );

  /** The languages as switcher options, named in the reader's language. */
  readonly localeOptions = computed<readonly ReferenceOption[]>(() =>
    this.locales().codes.map((code) => ({code, label: this.reference.languageName(code)})),
  );

  readonly counts = computed<Readonly<Record<string, number>>>(() => this.summary()?.counts ?? {});

  readonly context = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate('content.hub.context', {
      store: this.shell.currentStore()?.name ?? '',
    });
  });

  readonly sections = computed<readonly NavSection[]>(() => {
    this.transloco.activeLang();
    const counts = this.counts();
    return CONTENT_TABS.map((tab) => ({
      key: tab,
      labelKey: `content.tab.${tab}`,
      icon: TAB_ICONS[tab],
      count: counts[COUNT_KEYS[tab]] === undefined ? null : this.digits(counts[COUNT_KEYS[tab]]),
    }));
  });

  readonly kpis = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const s = this.summary();
    if (!s) {
      return [];
    }
    const locales = Object.entries(s.awaitingTranslation.byLocale ?? {})
      .map(([code, count]) => `${code.toUpperCase()} ${this.digits(count)}`)
      .join(' · ');
    const used = this.bytes(s.media.bytesUsed);
    const quota = this.bytes(s.media.bytesQuota);
    const percent = s.media.bytesQuota
      ? Math.round((s.media.bytesUsed / s.media.bytesQuota) * 100)
      : 0;
    return [
      {
        label: this.transloco.translate('content.kpi.published'),
        value: this.digits(s.publishedItems),
        icon: 'checkCircle',
        tone: 'green',
        flag: this.transloco.translate('content.kpi.publishedMeta'),
      },
      {
        label: this.transloco.translate('content.kpi.drafts'),
        value: this.digits(s.drafts.total),
        icon: 'pencil',
        tone: 'slate',
        flag: this.transloco.translate('content.kpi.draftsMeta', {
          count: s.drafts.staleOver30Days,
        }),
      },
      {
        label: this.transloco.translate('content.kpi.awaitingTranslation'),
        value: this.digits(s.awaitingTranslation.total),
        icon: 'globe',
        tone: 'amber',
        flag: locales || this.transloco.translate('content.kpi.allTranslated'),
      },
      {
        label: this.transloco.translate('content.kpi.media'),
        value: used,
        icon: 'images',
        tone: 'blue',
        flag: this.transloco.translate('content.kpi.mediaMeta', {
          count: s.media.fileCount,
          percent: this.digits(percent),
          quota,
        }),
      },
    ];
  });

  retry(): void {
    this.summaryResource.reload();
  }

  /** Called after any write so the KPIs and rail counts catch up. */
  invalidate(): void {
    this.cache.invalidate();
  }

  private digits(value: number): string {
    return this.localeFormat.localizeNumber(value, 'decimal');
  }

  private bytes(value: number): string {
    const gb = value / 1024 ** 3;
    if (gb >= 1) {
      return this.transloco.translate('content.bytes.gb', {
        value: this.localeFormat.localizeNumber(gb, 'decimal', undefined, {
          maximumFractionDigits: 1,
        }),
      });
    }
    const mb = value / 1024 ** 2;
    if (mb >= 1) {
      return this.transloco.translate('content.bytes.mb', {
        value: this.localeFormat.localizeNumber(mb, 'decimal', undefined, {
          maximumFractionDigits: 1,
        }),
      });
    }
    return this.transloco.translate('content.bytes.kb', {
      value: this.digits(Math.round(value / 1024)),
    });
  }
}
