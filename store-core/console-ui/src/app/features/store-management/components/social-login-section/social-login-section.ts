import {Component, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {SecretField} from '@shared/ui/secret-field/secret-field';
import {Toggle} from '@shared/ui/toggle/toggle';
import {
  LOGIN_PROVIDER_LABEL_KEY,
  LOGIN_STATE_TAG,
  isLoginProvider,
  type SocialLoginConfig,
} from '@models/store-settings';
import type {
  LoginProviderForm,
  SocialLoginForm,
} from '../../services/store-settings-form.service';

/**
 * Who a shopper can sign in with.
 *
 * The providers are `cua`'s `SocialProvider` — Google, Facebook and GitHub. The mockup's Apple
 * row is gone; GitHub, which the enum does carry, is here instead.
 *
 * A disabled provider collapses its credentials.
 *
 * The secret **is** shown, behind a reveal toggle, and that is a deliberate answer to what the API
 * does rather than a relaxation. `SocialLoginConfigMapper.toDTO` decrypts `appSecret` before
 * serialising, so the browser has already been handed the live value by the time this renders —
 * masking it as unknowable would be theatre, and would leave an operator unable to check a key they
 * can read in the network tab. See lessons.md, "Store management — payment and social-login reads
 * return secrets in cleartext".
 */
@Component({
  selector: 'app-social-login-section',
  imports: [Badge, CopyField, Icon, Panel, ReactiveFormsModule, SecretField, Toggle, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.socialLogin.title')"
      [subtitle]="t('storeSettings.socialLogin.subtitle')"
      *transloco="let t"
    >
      <div class="section-body providers" [formGroup]="form()">
        @for (provider of providers(); track provider.providerId) {
          @let group = groupOf(provider.providerId);
          @let on = group.controls.enabled.value;

          <div class="provider-card" [class.on]="on" [formGroupName]="provider.providerId">
            <div class="provider-head">
              <app-icon [name]="provider.icon" />
              <span class="provider-name">
                <strong>{{ labelOf(provider, t) }}</strong>
                @if (!provider.configured) {
                  <small>{{ t('storeSettings.socialLogin.notConfigured') }}</small>
                }
              </span>
              <app-badge [tone]="tagOf(on).tone" shape="square">{{ t(tagOf(on).labelKey) }}</app-badge>
              <app-toggle
                [name]="t('storeSettings.socialLogin.signInWith', {provider: labelOf(provider, t)})"
                [checked]="on"
                (checkedChange)="setFlag(group.controls.enabled, $event)"
              />
            </div>

            @if (on) {
              <div class="provider-body">
                <div class="field">
                  <label [attr.for]="'app-id-' + provider.providerId">{{ t('storeSettings.socialLogin.appId') }}</label>
                  <input
                    [id]="'app-id-' + provider.providerId"
                    class="control mono"
                    type="text"
                    formControlName="appId"
                  />
                </div>

                <div class="field">
                  <label [attr.for]="'app-secret-' + provider.providerId">
                    {{ t('storeSettings.socialLogin.appSecret') }}
                  </label>
                  <app-secret-field
                    [label]="t('storeSettings.socialLogin.appSecret')"
                    [value]="group.controls.appSecret.value"
                  >
                    <input
                      [id]="'app-secret-' + provider.providerId"
                      type="text"
                      autocomplete="off"
                      formControlName="appSecret"
                    />
                  </app-secret-field>
                  <p class="field-hint">{{ t('storeSettings.socialLogin.appSecretHint') }}</p>
                </div>

                @if (missingCredentials(group)) {
                  <p class="cross-field-error field-wide" role="alert">
                    {{ t('storeSettings.socialLogin.credentialsRequired') }}
                  </p>
                }

                <div class="field field-wide">
                  <p class="field-label">{{ t('storeSettings.socialLogin.callbackUrl') }}</p>
                  <app-copy-field [value]="provider.callbackUrl" [label]="t('storeSettings.socialLogin.callbackUrl')" />
                  <p class="field-hint">
                    {{ t('storeSettings.socialLogin.callbackHint') }}
                  </p>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css'],
})
export class SocialLoginSection {
  readonly form = input.required<SocialLoginForm>();
  readonly providers = input.required<readonly SocialLoginConfig[]>();

  /** A brand name for a provider the console knows; cua's own token for one it does not. */
  protected labelOf(provider: SocialLoginConfig, t: (key: string) => string): string {
    return isLoginProvider(provider.providerId)
      ? t(LOGIN_PROVIDER_LABEL_KEY[provider.providerId])
      : provider.providerId;
  }

  protected groupOf(provider: string): LoginProviderForm {
    return this.form().controls[provider];
  }

  protected tagOf(enabled: boolean) {
    return enabled ? LOGIN_STATE_TAG.on : LOGIN_STATE_TAG.off;
  }

  /**
   * Whether an enabled provider is missing what it needs to broker a sign-in.
   *
   * Said whenever it is true, including for a row that arrived that way — a provider switched on
   * with no app id is a sign-in button that fails for the shopper, and the seller should know even
   * if they did not cause it. Whether it also *blocks* the save is the validator's business, and it
   * only does so once the operator has touched the provider.
   */
  protected missingCredentials(group: LoginProviderForm): boolean {
    if (!group.controls.enabled.value) {
      return false;
    }
    return !group.controls.appId.value.trim() || !group.controls.appSecret.value.trim();
  }

  /**
   * Flips the switch, and says it was a change *first*.
   *
   * Order matters here in a way it does not elsewhere. `setValue` runs the group's validators
   * synchronously, and the credential rule only bites on a group the operator has touched — so
   * marking dirty afterwards means the first flip validates as untouched and the message does not
   * appear until something else changes.
   */
  protected setFlag(control: FormControl<boolean>, value: boolean): void {
    control.markAsDirty();
    control.setValue(value);
  }
}
