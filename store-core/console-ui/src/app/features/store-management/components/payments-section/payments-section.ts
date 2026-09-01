import {Component, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge, CopyField, Icon, NoticeBar, Panel, FormField, SecretField, TextField, Toggle} from '@cvhome-saas/ui-kit/ui';
import {
  GATEWAY_STATE_TAG,
  PAYMENT_TYPE_DESCRIPTION_KEY,
  PAYMENT_TYPE_LABEL_KEY,
  isPaymentType,
  type PaymentGatewayConfig,
} from '@models/store-settings';
import type {GatewayForm, PaymentsForm} from '../../services/store-settings-form.service';

/**
 * The gateways this store accepts, and the credentials the ones that need them hold.
 *
 * `PaymentType` decides which card has a credential grid: only `STRIPE` and `PAYPAL` declare
 * `attrs`, so `COD` and `MANUAL_TRANSFER` are a switch and nothing more. The mockup's Tap
 * Payments row is gone — the enum has no member for it.
 *
 * Both secrets **are** shown, behind a reveal toggle. They are encrypted at rest and
 * `PaymentConfigurationMapper.toDTO` decrypts all three fields before serialising, so the browser
 * already holds the live Stripe secret key by the time this renders — masking it as unknowable
 * would be theatre, and would leave an operator unable to check a key they can read in the network
 * tab. See lessons.md, "Store management — payment and social-login reads return secrets in
 * cleartext".
 */
@Component({
  selector: 'app-payments-section',
  imports: [
    Badge,
    CopyField,
    FormField,
    Icon,
    NoticeBar,
    Panel,
    ReactiveFormsModule,
    SecretField,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
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
                <strong>{{ labelOf(gateway, t) }}</strong>
                <small>{{ descriptionOf(gateway, t) }}</small>
              </span>
              <app-badge [tone]="tagOf(on).tone" shape="square">{{ t(tagOf(on).labelKey) }}</app-badge>
              <app-toggle
                [name]="t('storeSettings.payments.accept', {gateway: labelOf(gateway, t)})"
                [checked]="on"
                (checkedChange)="setFlag(group.controls.enabled, $event)"
              />
            </div>

            <!-- COD and MANUAL_TRANSFER declare no attrs, so they have no grid to expand. -->
            @if (on) {
              @if (gateway.credentials; as credentials) {
              <div class="provider-body">
                <app-form-field
                  [label]="t('storeSettings.payments.apiKey')"
                  [hint]="t('storeSettings.payments.publishable')"
                  [controlId]="'api-key-' + gateway.paymentType"
                >
                  <app-text-field
                    mono
                    [id]="'api-key-' + gateway.paymentType"
                    formControlName="apiKey"
                    latin
                    autocomplete="off"
                  />
                </app-form-field>

                <app-form-field [label]="t('storeSettings.payments.secretKey')">
                  <app-secret-field
                    [label]="t('storeSettings.payments.secretKey')"
                    [value]="group.controls.secretKey.value"
                  >
                    <input
                      [id]="'secret-key-' + gateway.paymentType"
                      type="text"
                      autocomplete="off"
                      formControlName="secretKey"
                    />
                  </app-secret-field>
                </app-form-field>

                <app-form-field
                  wide
                  [label]="t('storeSettings.payments.webhookSecret')"
                  [hint]="t('storeSettings.payments.webhookSecretHint')"
                >
                  <app-secret-field
                    [label]="t('storeSettings.payments.webhookSecret')"
                    [value]="group.controls.webhookSecret.value"
                  >
                    <input
                      [id]="'webhook-secret-' + gateway.paymentType"
                      type="text"
                      autocomplete="off"
                      formControlName="webhookSecret"
                    />
                  </app-secret-field>
                </app-form-field>

                <!--
                  The endpoint the gateway posts to. Not returned by anything — assembled from the
                  route PublicPaymentWebhookApi actually maps — which is why it is read-only and
                  copyable rather than a field: it is the platform's address, not the seller's.
                -->
                <div class="field field-wide">
                  <p class="field-label">{{ t('storeSettings.payments.webhookUrl') }}</p>
                  <app-copy-field
                    [value]="credentials.webhookUrl"
                    [label]="t('storeSettings.payments.webhookUrl')"
                  />
                  <p class="field-hint">{{ t('storeSettings.payments.webhookUrlHint', {gateway: labelOf(gateway, t)}) }}</p>
                </div>
              </div>
              }
            }
          </div>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../../../../shared/styles/field.css', '../settings-card.css', './payments-section.css'],
})
export class PaymentsSection {
  readonly form = input.required<PaymentsForm>();
  readonly gateways = input.required<readonly PaymentGatewayConfig[]>();

  /** Real copy for a gateway the console knows; the server's own token for one it does not. */
  protected labelOf(gateway: PaymentGatewayConfig, t: (key: string) => string): string {
    return isPaymentType(gateway.paymentType)
      ? t(PAYMENT_TYPE_LABEL_KEY[gateway.paymentType])
      : gateway.paymentType;
  }

  protected descriptionOf(gateway: PaymentGatewayConfig, t: (key: string) => string): string {
    return isPaymentType(gateway.paymentType)
      ? t(PAYMENT_TYPE_DESCRIPTION_KEY[gateway.paymentType])
      : t('storeSettings.payments.unknownGateway');
  }

  protected groupOf(paymentType: string): GatewayForm {
    return this.form().controls[paymentType];
  }

  protected tagOf(enabled: boolean) {
    return enabled ? GATEWAY_STATE_TAG.on : GATEWAY_STATE_TAG.off;
  }

  protected setFlag(control: FormControl<boolean>, value: boolean): void {
    control.setValue(value);
    control.markAsDirty();
  }
}
