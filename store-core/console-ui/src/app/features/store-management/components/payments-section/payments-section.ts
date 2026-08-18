import {Component, inject, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';
import {
  GATEWAY_STATE_TAG,
  PAYMENT_TYPE_DESCRIPTION_KEY,
  PAYMENT_TYPE_LABEL_KEY,
  type PaymentGatewayConfig,
  type SecretHint,
} from '@models/store-settings';
import type {GatewayForm, PaymentsForm} from '../../services/store-settings-form.service';

/**
 * The gateways this store accepts, and the credentials the ones that need them hold.
 *
 * `PaymentType` decides which card has a credential grid: only `STRIPE` and `PAYPAL` declare
 * `attrs`, so `COD` and `MANUAL_TRANSFER` are a switch and nothing more. The mockup's Tap
 * Payments row is gone — the enum has no member for it.
 *
 * Neither secret is ever shown. Both are encrypted at rest and never returned, so each field
 * states what is on record and offers the one action that can change it.
 */
@Component({
  selector: 'app-payments-section',
  imports: [Badge, Icon, NoticeBar, Panel, ReactiveFormsModule, Toggle, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.payments.title')"
      [subtitle]="t('storeSettings.payments.subtitle')"
      *transloco="let t"
    >
      <div class="section-body providers" [formGroup]="form()">
        <app-notice-bar
          class="secrets-notice"
          tone="blue"
          icon="shield"
          [message]="t('storeSettings.payments.secretsNotice')"
        />

        @for (gateway of gateways(); track gateway.paymentType) {
          @let group = groupOf(gateway.paymentType);
          @let on = group.controls.enabled.value;

          <div class="provider-card" [class.on]="on" [formGroupName]="gateway.paymentType">
            <div class="provider-head">
              <app-icon [name]="gateway.icon" />
              <span class="provider-name">
                <strong>{{ t(labelKeyOf(gateway)) }}</strong>
                <small>{{ t(descriptionKeyOf(gateway)) }}</small>
              </span>
              <app-badge [tone]="tagOf(on).tone" shape="square">{{ t(tagOf(on).labelKey) }}</app-badge>
              <app-toggle
                [name]="t('storeSettings.payments.accept', {gateway: t(labelKeyOf(gateway))})"
                [checked]="on"
                (checkedChange)="setFlag(group.controls.enabled, $event)"
              />
            </div>

            <!-- COD and MANUAL_TRANSFER declare no attrs, so they have no grid to expand. -->
            @if (on) {
              @if (gateway.credentials; as credentials) {
              <div class="provider-body">
                <div class="field">
                  <label [attr.for]="'api-key-' + gateway.paymentType">
                    {{ t('storeSettings.payments.apiKey') }} <span class="field-note">{{ t('storeSettings.payments.publishable') }}</span>
                  </label>
                  <input
                    [id]="'api-key-' + gateway.paymentType"
                    class="control mono"
                    type="text"
                    formControlName="apiKey"
                  />
                </div>

                <div class="field">
                  <p class="field-label">{{ t('storeSettings.payments.secretKey') }}</p>
                  <span class="control secret">
                    <app-icon name="lock" />
                    <span class="mask">{{ maskOf(credentials.secretKey, t) }}</span>
                    <button
                      class="text-action"
                      type="button"
                      (click)="notSupported(t('storeSettings.payments.rotatingSecret', {gateway: t(labelKeyOf(gateway))}))"
                    >
                      {{ t('storeSettings.payments.rotate') }}
                    </button>
                  </span>
                  <p class="field-hint">{{ rotatedOf(credentials.secretKey, t) }}</p>
                </div>

                <div class="field field-wide">
                  <p class="field-label">{{ t('storeSettings.payments.webhookSecret') }}</p>
                  <span class="control secret">
                    <app-icon name="lock" />
                    <span class="mask">{{ maskOf(credentials.webhookSecret, t) }}</span>
                    <button
                      class="text-action"
                      type="button"
                      (click)="notSupported(t('storeSettings.payments.replacingWebhookSecret', {gateway: t(labelKeyOf(gateway))}))"
                    >
                      {{ t('storeSettings.socialLogin.replace') }}
                    </button>
                  </span>
                  <p class="field-hint endpoint">
                    <app-icon name="link" />
                    {{ t('storeSettings.payments.endpoint', {url: credentials.webhookUrl}) }}
                  </p>
                </div>
              </div>
              }
            }
          </div>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './payments-section.css'],
})
export class PaymentsSection {
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly form = input.required<PaymentsForm>();
  readonly gateways = input.required<readonly PaymentGatewayConfig[]>();

  protected labelKeyOf(gateway: PaymentGatewayConfig): string {
    return PAYMENT_TYPE_LABEL_KEY[gateway.paymentType];
  }

  protected descriptionKeyOf(gateway: PaymentGatewayConfig): string {
    return PAYMENT_TYPE_DESCRIPTION_KEY[gateway.paymentType];
  }

  protected groupOf(paymentType: string): GatewayForm {
    return this.form().controls[paymentType];
  }

  protected tagOf(enabled: boolean) {
    return enabled ? GATEWAY_STATE_TAG.on : GATEWAY_STATE_TAG.off;
  }

  protected maskOf(secret: SecretHint, t: (key: string, params?: Record<string, unknown>) => string): string {
    return secret.endsWith
      ? t('storeSettings.secret.endsWith', {last4: secret.endsWith})
      : t('storeSettings.secret.notSet');
  }

  protected rotatedOf(secret: SecretHint, t: (key: string, params?: Record<string, unknown>) => string): string {
    return secret.lastRotated
      ? t('storeSettings.secret.lastRotated', {date: secret.lastRotated})
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
