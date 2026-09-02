import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  FormField,
  LoadError,
  NoticeBar,
  NumberField,
  PageHeader,
  Panel,
  Select,
  TextField,
  Toggle,
  Badge,
  ConfirmDialog,
  CopyField,
  DataTable,
  FormDialog,
  TableRow,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import {SETTINGS_SECTIONS, SettingsFacade} from './facades/settings.facade';

/**
 * Realm-wide policy. Six sections in the design; four are built (General, Authentication, Sessions
 * & tokens, Signing keys' interval). Email is not — the platform has no mail sender, links are
 * delivered by an event a future service subscribes to — and the danger zone arrives with the
 * sessions and clients work that gives its buttons something to do.
 */
@Component({
  selector: 'app-settings',
  imports: [
    ReactiveFormsModule,
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    FormField,
    TextField,
    NumberField,
    Toggle,
    Select,
    ConfirmDialog,
    FormDialog,
    CopyField,
    DataTable,
    TableRow,
    Badge,
  ],
  providers: [SettingsFacade],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
  /** The key table: kid, algorithm, status, when it started signing, when it leaves the JWKS. */
  private readonly keyKeys: readonly {key: string; width: string}[] = [
    {key: 'kid', width: 'minmax(14rem, 2fr)'},
    {key: 'algorithm', width: 'minmax(5rem, 0.6fr)'},
    {key: 'status', width: 'minmax(6rem, 0.8fr)'},
    {key: 'activated', width: 'minmax(9rem, 1fr)'},
    {key: 'retire', width: 'minmax(9rem, 1fr)'},
  ];

  protected keyColumns(t: (key: string) => string): readonly TableColumn[] {
    return this.keyKeys.map(({key, width}) => ({key, width, label: t('settings.keys.column.' + key)}));
  }

  protected when(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleString() : '—';
  }

  protected readonly facade = inject(SettingsFacade);
  protected readonly sections = SETTINGS_SECTIONS;
}
