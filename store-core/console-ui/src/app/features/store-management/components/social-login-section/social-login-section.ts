import {Component, inject, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';
import {LOGIN_PROVIDER_LABEL_KEY, LOGIN_STATE_TAG, type SecretHint, type SocialLoginConfig} from '@models/store-settings';
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
 * A disabled provider collapses its credentials. Its `appSecret` is never shown, because it is
 * encrypted at rest and never returned — the field says how the stored one ends and offers to
 * replace it.
 */
@Component({
  selector: 'app-social-login-section',
  imports: [Badge, CopyField, Icon, Panel, ReactiveFormsModule, Toggle, TranslocoDirective],
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
                <strong>{{ t(labelKeyOf(provider)) }}</strong>
              </span>
              <app-badge [tone]="tagOf(on).tone" shape="square">{{ t(tagOf(on).labelKey) }}</app-badge>
              <app-toggle
                [name]="t('storeSettings.socialLogin.signInWith', {provider: t(labelKeyOf(provider))})"
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
                  <p class="field-label">{{ t('storeSettings.socialLogin.appSecret') }}</p>
                  <span class="control secret">
                    <app-icon name="lock" />
                    <span class="mask">{{ maskOf(provider.appSecret, t) }}</span>
                    <button
                      class="text-action"
                      type="button"
                      (click)="notSupported(t('storeSettings.socialLogin.replacingSecret', {provider: t(labelKeyOf(provider))}))"
                    >
                      {{ t('storeSettings.socialLogin.replace') }}
                    </button>
                  </span>
                  <p class="field-hint">{{ rotatedOf(provider.appSecret, t) }}</p>
                </div>

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
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly form = input.required<SocialLoginForm>();
  readonly providers = input.required<readonly SocialLoginConfig[]>();

  protected labelKeyOf(provider: SocialLoginConfig): string {
    return LOGIN_PROVIDER_LABEL_KEY[provider.providerId];
  }

  protected groupOf(provider: string): LoginProviderForm {
    return this.form().controls[provider];
  }

  protected tagOf(enabled: boolean) {
    return enabled ? LOGIN_STATE_TAG.on : LOGIN_STATE_TAG.off;
  }

  /** What is on record, never the value itself. */
  protected maskOf(secret: SecretHint, t: (key: string, params?: Record<string, unknown>) => string): string {
    return secret.endsWith
      ? t('storeSettings.secret.endsWith', {last4: secret.endsWith})
      : t('storeSettings.secret.notSet');
  }

  protected rotatedOf(secret: SecretHint, t: (key: string, params?: Record<string, unknown>) => string): string {
    return secret.lastRotated
      ? t('storeSettings.secret.lastChanged', {date: secret.lastRotated})
      : t('storeSettings.secret.neverStored');
  }

  protected setFlag(control: FormControl<boolean>, value: boolean): void {
    control.setValue(value);
    control.markAsDirty();
  }

  protected notSupported(what: string): void {
    this.toast.info(this.transloco.translate('storeSettings.notAvailable', {what}));
  }
}
