import {Component, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {
  SOCIAL_LINK_LABEL_KEY,
  isSocialLinkProvider,
  type SocialLinkSetting,
} from '@models/store-settings';
import type {SocialLinksForm} from '../../services/store-settings-form.service';

/**
 * The storefront footer's profile links, one row per provider.
 *
 * The rows are whatever `GET /public/social-links-providers` answers with — `SocialProvider`'s five
 * members today, and the mockup's LinkedIn and YouTube are not among them. A provider the enum grows
 * later renders on its own name rather than throwing under Transloco's strict missing handler, the
 * known-set discipline `shared/i18n/status-label.ts` sets.
 */
@Component({
  selector: 'app-social-links-section',
  imports: [FieldError, Icon, Panel, ReactiveFormsModule, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.social.title')"
      [subtitle]="t('storeSettings.social.subtitle')"
      *transloco="let t"
    >
      <div class="section-body links" [formGroup]="form()">
        @for (link of links(); track link.provider) {
          <div class="link-row">
            <label class="link-name" [attr.for]="'social-' + link.provider">
              <app-icon [name]="link.icon" />
              <span>{{ labelOf(link, t) }}</span>
            </label>

            <div class="link-field">
              <span class="control">
                <input
                  [id]="'social-' + link.provider"
                  type="url"
                  [formControlName]="link.provider"
                  [placeholder]="t('storeSettings.social.addProfileUrl')"
                />
                @if (isSet(link.provider)) {
                  <app-icon name="checkCircle" class="ok" />
                }
              </span>
              <app-field-error
                [control]="controlOf(link.provider)"
                [fallback]="t('storeSettings.social.fallback')"
              />
            </div>
          </div>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './social-links-section.css'],
})
export class SocialLinksSection {
  readonly form = input.required<SocialLinksForm>();
  readonly links = input.required<readonly SocialLinkSetting[]>();

  /** A brand name for a provider the console knows; the server's own token for one it does not. */
  protected labelOf(link: SocialLinkSetting, t: (key: string) => string): string {
    return isSocialLinkProvider(link.provider)
      ? t(SOCIAL_LINK_LABEL_KEY[link.provider])
      : link.provider;
  }

  protected controlOf(provider: string): FormControl<string> {
    return this.form().controls[provider];
  }

  /** A filled, valid row earns the tick. Methods, because control state is not reactive. */
  protected isSet(provider: string): boolean {
    const control = this.controlOf(provider);
    return control.value.trim().length > 0 && control.valid;
  }
}
