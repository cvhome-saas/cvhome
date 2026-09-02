import {Component, inject} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  Badge,
  BusyOverlay,
  Checkbox,
  ConfirmDialog,
  DurationField,
  FieldError,
  FormField,
  Icon,
  LoadError,
  NoticeBar,
  OneTimeLinkDialog,
  PageHeader,
  Panel,
  Select,
  TextField,
  TextareaField,
  Toggle,
  type SelectOption,
} from '@cvhome-saas/ui-kit/ui';

import {PairList} from '@shared/ui/pair-list/pair-list';
import {UriList} from './components/uri-list/uri-list';
import {ClientFormFacade} from './facades/client-form.facade';

@Component({
  selector: 'app-client-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    TranslocoDirective,
    PageHeader,
    Panel,
    NoticeBar,
    BusyOverlay,
    LoadError,
    FormField,
    FieldError,
    TextField,
    Select,
    Checkbox,
    Toggle,
    DurationField,
    Icon,
    ConfirmDialog,
    OneTimeLinkDialog,
    Badge,
    TextareaField,
    UriList,
    PairList,
  ],
  providers: [ClientFormFacade],
  templateUrl: './client-form.html',
  styleUrl: './client-form.css',
})
export class ClientForm {
  protected readonly facade = inject(ClientFormFacade);

  /** A `string[]` control read as a checkbox group. The template needs the narrowed type. */
  protected picks(name: 'clientAuthenticationMethods' | 'authorizationGrantTypes' | 'scopes'): FormControl<
    readonly string[]
  > {
    return this.facade.form.controls[name];
  }

  /**
   * An enum list as select options, with a leading blank.
   *
   * Every one of these fields is optional on the server — `null` means "the authorization server's
   * default" — and a select with no way back to unset would make the first accidental pick permanent.
   */
  protected optional(values: readonly string[], blankLabel: string): readonly SelectOption[] {
    return [{value: '', label: blankLabel}, ...values.map((value) => ({value, label: value}))];
  }

  protected when(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleString() : '—';
  }

  protected units(t: (key: string) => string): readonly SelectOption[] {
    return [
      {value: 's', label: t('clientForm.unit.seconds')},
      {value: 'm', label: t('clientForm.unit.minutes')},
      {value: 'h', label: t('clientForm.unit.hours')},
      {value: 'd', label: t('clientForm.unit.days')},
    ];
  }
}
