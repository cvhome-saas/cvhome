import {Component, computed, inject, input, output} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {
  DOMAIN_STATUS_COPY,
  DOMAIN_STATUS_TONE,
  type DnsRecord,
  type DomainStatus,
} from '@models/store-settings';
import type {DomainForm} from '../../services/store-settings-form.service';

/**
 * The platform subdomain, the operator's own domain, and where its DNS check stands.
 *
 * The five states are drawn from the `Tone` vocabulary rather than the mockup's five
 * hardcoded palettes, so they follow the theme and mean the same thing here as an amber
 * badge means anywhere else in the console.
 */
@Component({
  selector: 'app-domain-section',
  imports: [Badge, CopyField, FieldError, Icon, Panel, ReactiveFormsModule, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.domain.title')"
      [subtitle]="t('storeSettings.domain.subtitle')"
      *transloco="let t"
    >
      <div class="section-body" [formGroup]="form()">
        <div class="info-row">
          <app-icon name="globe" />
          <span class="info-copy">
            <strong>{{ subdomain() }}</strong>
            <small>{{ t('storeSettings.domain.defaultSubdomain') }}</small>
          </span>
          <app-badge tone="slate" shape="square">{{ t('storeSettings.domain.system') }}</app-badge>
        </div>

        <div class="field">
          <label for="custom-domain">{{ t('storeSettings.domain.customDomain') }}</label>
          <div class="domain-row">
            <span class="control" [class.invalid]="domainInvalid()">
              <span class="scheme">{{ scheme }}</span>
              <input id="custom-domain" type="text" formControlName="customDomain" />
              <app-icon [name]="copy().icon" [class]="tone()" />
            </span>
            <button class="primary-action" type="button" (click)="verify.emit()">{{ t('storeSettings.domain.verify') }}</button>
          </div>
          <p class="field-hint">{{ t('storeSettings.domain.pattern') }}</p>
          <app-field-error
            [control]="form().controls.customDomain"
            [fallback]="t('storeSettings.domain.fallback')"
          />
        </div>

        <div class="status" [class]="tone()">
          <div class="status-head">
            <app-icon [name]="copy().icon" />
            <strong>{{ t(copy().titleKey) }}</strong>
            <span class="status-meta">{{ t(copy().metaKey) }}</span>
          </div>
          <p class="status-body">{{ body(t) }}</p>
        </div>

        @if (record(); as dns) {
          <div class="field">
            <p class="field-label">{{ t('storeSettings.domain.recordToAdd') }}</p>
            <div class="dns">
              <div class="dns-head">
                <span>{{ t('storeSettings.domain.dnsType') }}</span><span>{{ t('storeSettings.domain.dnsName') }}</span><span>{{ t('storeSettings.domain.dnsValue') }}</span><span>{{ t('storeSettings.domain.dnsTtl') }}</span>
              </div>
              <div class="dns-row">
                <strong>{{ dns.type }}</strong>
                <span>{{ dns.name }}</span>
                <app-copy-field [value]="dns.value" [label]="t('storeSettings.domain.dnsValue')" />
                <span>{{ dns.ttl }}</span>
              </div>
            </div>
            <p class="field-hint dns-hint">
              <app-icon name="questionCircle" />
              {{ t('storeSettings.domain.dnsHint') }}
            </p>
          </div>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './domain-section.css'],
})
export class DomainSection {
  private readonly transloco = inject(TranslocoService);

  // A URL scheme, not language-dependent text.
  protected readonly scheme = 'https://';

  readonly form = input.required<DomainForm>();
  readonly subdomain = input.required<string>();
  readonly status = input.required<DomainStatus>();
  readonly record = input.required<DnsRecord | null>();

  readonly verify = output<void>();

  protected readonly tone = computed(() => DOMAIN_STATUS_TONE[this.status()]);
  protected readonly copy = computed(() => DOMAIN_STATUS_COPY[this.status()]);

  /**
   * The status copy names the host it is talking about, so the panel fills it in.
   *
   * A method rather than a computed: `value` and `invalid` are not signals, so a computed
   * would latch onto whatever the control held on first render. Read per change-detection
   * pass instead, the way `app-field-error` reads its own control.
   */
  protected body(t: (key: string, params?: Record<string, unknown>) => string): string {
    const domain = this.form().controls.customDomain.value;
    return t(this.copy().bodyKey, {domain: domain || this.transloco.translate('storeSettings.domain.thisDomain')});
  }

  protected domainInvalid(): boolean {
    const control = this.form().controls.customDomain;
    return control.invalid && (control.dirty || control.touched);
  }
}
