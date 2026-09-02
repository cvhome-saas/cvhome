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
  ],
  providers: [SettingsFacade],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings {
  protected readonly facade = inject(SettingsFacade);
  protected readonly sections = SETTINGS_SECTIONS;
}
