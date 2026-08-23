import {Component, computed, effect, inject, input, output} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import type {
  BulkAction,
  ContentListType,
  ContentRow,
  ContentStatus,
  TransitionAction,
} from '@models/content';
import {ActionMenu, type MenuAction} from '@shared/ui/action-menu/action-menu';
import {Badge} from '@shared/ui/badge/badge';
import type {BadgeTone} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {Checkbox} from '@shared/ui/checkbox/checkbox';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {TableRow} from '@shared/ui/data-table/table-row';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';
import {LoadError} from '@shared/ui/load-error/load-error';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {
  CONTENT_PAGE_SIZE,
  ContentListFacade,
  type StatusTab,
} from '../../facades/content-list.facade';
import {LocaleBadges} from '../locale-badges/locale-badges';

export const STATUS_TONES: Readonly<Record<ContentStatus, BadgeTone>> = {
  PUBLISHED: 'green',
  DRAFT: 'slate',
  SCHEDULED: 'amber',
  REVIEW: 'blue',
  ARCHIVED: 'red',
};

const ROW_ICONS: Readonly<Record<ContentListType, IconName>> = {
  pages: 'file',
  posts: 'messageCircle',
  banners: 'images',
  faq: 'questionCircle',
  policies: 'shield',
};

const TRANSITION_ICONS: Readonly<Record<TransitionAction, IconName>> = {
  publish: 'checkCircle',
  unpublish: 'eyeOff',
  'submit-review': 'send',
  archive: 'box',
  restore: 'undo',
};

/**
 * The list every workflow tab shares — `Content Management.dc.html`'s list view: status chips and a
 * language filter on the panel header, a table of title · status · languages · updated, a row menu
 * with the transitions the row's status allows, a bulk bar when rows are ticked, and the pager.
 */
@Component({
  selector: 'app-content-list',
  imports: [
    ActionMenu,
    Badge,
    BusyOverlay,
    Checkbox,
    ConfirmDialog,
    DataTable,
    EmptyState,
    Icon,
    LoadError,
    LocaleBadges,
    Pagination,
    Panel,
    SearchBox,
    Select,
    TabSwitcher,
    TableRow,
    TranslocoDirective,
  ],
  providers: [ContentListFacade],
  templateUrl: './content-list.html',
  styleUrl: './content-list.css',
})
export class ContentList {
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  protected readonly facade = inject(ContentListFacade);

  readonly type = input.required<ContentListType>();
  readonly locales = input.required<readonly ReferenceOption[]>();
  /** Whether the operator may write; read-only roles see no checkboxes, menus or create buttons. */
  readonly canManage = input(true);

  /** The operator wants to open a row — the hub routes to the right editor. */
  readonly open = output<ContentRow>();
  readonly create = output<void>();

  protected readonly pageSize = CONTENT_PAGE_SIZE;

  constructor() {
    effect(() => this.facade.setType(this.type()));
  }

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    const manage = this.canManage();
    return [
      ...(manage ? [{key: 'select', label: '', width: '2.25rem'}] : []),
      {
        key: 'title',
        label: this.transloco.translate(`content.column.${this.type()}`),
        width: 'minmax(14rem, 2.4fr)',
      },
      {key: 'status', label: this.transloco.translate('content.column.status'), width: '7.5rem'},
      {
        key: 'languages',
        label: this.transloco.translate('content.column.languages'),
        width: 'minmax(7rem, 1.1fr)',
      },
      {
        key: 'updated',
        label: this.transloco.translate('content.column.updated'),
        width: 'minmax(8rem, 1.1fr)',
      },
      {key: 'actions', label: '', width: '5.5rem'},
    ];
  });

  protected readonly localeOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('content.filter.allLanguages')},
      ...this.locales().map((locale) => ({
        value: locale.code,
        label: this.transloco.translate('content.filter.missingLanguage', {
          language: locale.label,
        }),
      })),
    ];
  });

  protected readonly expectedCodes = computed(() => this.locales().map((locale) => locale.code));

  protected readonly rowIcon = computed<IconName>(() => ROW_ICONS[this.type()]);

  protected tone(status: ContentStatus): BadgeTone {
    return STATUS_TONES[status];
  }

  protected statusLabel(row: ContentRow): string {
    return this.transloco.translate(`content.status.${row.status}`);
  }

  /** "Publishes Aug 9" for a scheduled row, the modified date otherwise. */
  protected updatedLabel(row: ContentRow): string {
    this.transloco.activeLang();
    if (row.status === 'SCHEDULED' && row.publishAt) {
      return this.transloco.translate('content.list.publishesOn', {
        date: this.date(row.publishAt),
      });
    }
    return row.updatedAt ? this.date(row.updatedAt) : '—';
  }

  protected menuFor(row: ContentRow): readonly MenuAction[] {
    this.transloco.activeLang();
    const transitions = this.facade.transitionsFor(row).map((action): MenuAction => ({
      key: action,
      label: this.transloco.translate(`content.action.${action}`),
      icon: TRANSITION_ICONS[action],
    }));
    return [
      {key: 'edit', label: this.transloco.translate('content.action.edit'), icon: 'pencil'},
      ...transitions,
      {
        key: 'delete',
        label: this.transloco.translate('content.action.delete'),
        icon: 'trash',
        danger: true,
      },
    ];
  }

  protected onMenu(row: ContentRow, action: MenuAction): void {
    if (action.key === 'edit') {
      this.open.emit(row);
    } else if (action.key === 'delete') {
      this.facade.askDelete(row);
    } else {
      this.facade.transition(row, action.key as TransitionAction);
    }
  }

  protected onStatusTab(key: string): void {
    this.facade.statusTab.set(key as StatusTab);
  }

  protected onLocale(value: string): void {
    this.facade.locale.set(value === '' ? null : value);
  }

  protected bulk(action: BulkAction): void {
    this.facade.bulk(action);
  }

  protected deleteTitle(): string {
    const pending = this.facade.pendingDelete();
    return pending ? this.transloco.translate('content.delete.title', {title: pending.title}) : '';
  }

  protected selectedCount(): string {
    return this.localeFormat.localizeNumber(this.facade.selected().size, 'decimal');
  }

  private date(iso: string): string {
    return this.localeFormat.localizeDate(iso, undefined, {dateStyle: 'medium'});
  }
}
