import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  CopyField,
  DataTable,
  EmptyState,
  FormDialog,
  FormField,
  Icon,
  LoadError,
  NoticeBar,
  PageHeader,
  Panel,
  Select,
  TabSwitcher,
  TableRow,
  TextField,
  Toggle,
  type TabItem,
  type TableColumn,
} from '@cvhome-saas/ui-kit/ui';
import type {IdentityProviderDto, IdpPreset} from '@cvhome-saas/ui-kit/uaa';

import {PairList} from '@shared/ui/pair-list/pair-list';
import {IdentityProvidersFacade} from './facades/identity-providers.facade';

@Component({
  selector: 'app-identity-providers',
  imports: [
    ReactiveFormsModule,
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    EmptyState,
    DataTable,
    TableRow,
    Badge,
    Toggle,
    Icon,
    FormDialog,
    FormField,
    TextField,
    Select,
    Checkbox,
    CopyField,
    TabSwitcher,
    ConfirmDialog,
    PairList,
  ],
  providers: [IdentityProvidersFacade],
  templateUrl: './identity-providers.html',
  styleUrl: './identity-providers.css',
})
export class IdentityProviders {
  protected readonly facade = inject(IdentityProvidersFacade);

  private readonly keys: readonly {key: string; width: string}[] = [
    {key: 'provider', width: 'minmax(12rem, 2fr)'},
    {key: 'protocol', width: 'minmax(6rem, 0.7fr)'},
    {key: 'policy', width: 'minmax(9rem, 1.2fr)'},
    {key: 'status', width: 'minmax(6rem, 0.8fr)'},
    {key: 'order', width: '5rem'},
  ];

  protected columns(t: (key: string) => string): readonly TableColumn[] {
    return this.keys.map(({key, width}) => ({key, width, label: t('idps.column.' + key)}));
  }

  protected sections(t: (key: string) => string): readonly TabItem[] {
    return ['connection', 'mapping', 'behaviour'].map((key) => ({key, label: t('idps.section.' + key)}));
  }

  /** One glyph per preset: the initial of the provider, which is what every sign-in page draws. */
  protected glyph(preset: IdpPreset): string {
    switch (preset) {
      case 'GOOGLE':
        return 'G';
      case 'MICROSOFT':
        return 'M';
      case 'APPLE':
        return 'A';
      case 'GITHUB':
        return 'GH';
      default:
        return 'ID';
    }
  }

  protected policy(provider: IdentityProviderDto, t: (key: string) => string): string {
    const parts = [t('idps.linking.' + provider.accountLinking)];
    if (provider.jitProvisioning) {
      parts.push(t('idps.jitShort'));
    }
    if (provider.emailDomains.length) {
      parts.push(provider.emailDomains.join(', '));
    }
    return parts.join(' · ');
  }

  /** The row opens the dialog unless the click landed on a control inside it. */
  protected open(event: Event, provider: IdentityProviderDto): void {
    if ((event.target as HTMLElement).closest('.cell.status, .cell.order')) {
      return;
    }
    this.facade.select(provider);
  }
}
