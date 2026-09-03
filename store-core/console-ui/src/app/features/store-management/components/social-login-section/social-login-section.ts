import {Component, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge, CopyField, Icon, Panel, FormField, SecretField, TextField, Toggle} from '@cvhome-saas/ui-kit/ui';
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
 * The secret is never sent back. It used to be — the old mapper decrypted `appSecret` before
 * serialising, so masking it would have been theatre — but the read no longer returns it, and an
 * empty field now means "keep the stored one" rather than "no secret set". The app id is still
 * shown: an OAuth2 client id travels in the authorization URL and is public by construction, and a
 * merchant has to be able to see which application their store is wired to.
 */
@Component({
  selector: 'app-social-login-section',
  imports: [
    Badge,
    CopyField,
    FormField,
    Icon,
    Panel,
    ReactiveFormsModule,
    SecretField,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
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
                <app-form-field
                  [label]="t('storeSettings.socialLogin.appId')"
                  [controlId]="'app-id-' + provider.providerId"
                >
                  <app-text-field
                    mono
                    [id]="'app-id-' + provider.providerId"
                    formControlName="appId"
                    latin
                    autocomplete="off"
                  />
                </app-form-field>

                <app-form-field
                  [label]="t('storeSettings.socialLogin.appSecret')"
                  [hint]="t('storeSettings.socialLogin.appSecretHint')"
                >
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
                </app-form-field>

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
  styleUrls: ['../../../../shared/styles/field.css', '../settings-card.css'],
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
    const secretMissing = !group.controls.appSecret.value.trim() && !group.controls.hasAppSecret.value;
    return !group.controls.appId.value.trim() || secretMissing;
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
