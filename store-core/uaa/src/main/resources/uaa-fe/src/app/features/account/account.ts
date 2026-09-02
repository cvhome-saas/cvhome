import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {
  BusyOverlay,
  EmptyState,
  FormField,
  LoadError,
  NoticeBar,
  PageHeader,
  Panel,
  TextField,
} from '@cvhome-saas/ui-kit/ui';
import {AccountFacade} from './facades/account.facade';

/**
 * The signed-in person's own account. Profile facts come from `/me`; the two things a person can do here are
 * change their password and end their sessions. Anyone who can sign in to uaa reaches this page — it is the one
 * screen not behind the super-admin gate.
 */
@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule, TranslocoDirective, PageHeader, Panel, NoticeBar, BusyOverlay, LoadError, EmptyState, FormField, TextField],
  providers: [AccountFacade],
  templateUrl: './account.html',
  styleUrl: './account.css',
})
export class Account {
  protected readonly facade = inject(AccountFacade);

  protected when(value: string | null): string {
    return value ? new Date(value).toLocaleString() : '—';
  }
}
