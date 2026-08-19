import {Component, input, output} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {
  DOMAIN_STATUS_COPY,
  DOMAIN_STATUS_TONE,
  bareHostname,
  type DnsRecord,
  type DomainStatus,
  type StoreDomain,
} from '@models/store-settings';
import type {DomainForm} from '../../services/store-settings-form.service';

/**
 * The hostnames the storefront answers on.
 *
 * Three things, in the order an operator meets them: the address the store already has, the custom
 * domains it has been given, and the CNAME needed to add another. A store may hold any number of
 * custom domains — `GET /allocates` answers with a `Set` — which the fixture's single-domain shape
 * could not express.
 *
 * **The check is a browser-side DNS lookup, not a platform verification.** Nothing server-side ever
 * confirms the record; a green tick means one public resolver saw the CNAME a moment ago. The copy
 * says so, and allocation is deliberately *not* gated on it: registering a hostname before DNS
 * propagates is normal and works the moment it does. TODO(lessons.md): see lessons.md, "Store
 * management — DNS verification is a browser-side check, not a platform one".
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
            <!--
              No dir="ltr" here, and that is the point. It orders the characters correctly but also
              takes the element's alignment with it, so in an Arabic page the hostname jumped to the
              left edge of the row while the caption under it stayed on the right. The CSS uses
              unicode-bidi: plaintext instead: Latin-ordered text, page-aligned box.
            -->
            <strong>{{ subdomain() || t('storeSettings.domain.subdomainUnknown') }}</strong>
            <small>{{ t('storeSettings.domain.defaultSubdomain') }}</small>
          </span>
          @if (subdomain()) {
            <a class="text-action" [href]="'https://' + subdomain()" target="_blank" rel="noopener">
              {{ t('storeSettings.domain.open') }}
            </a>
          }
          <app-badge tone="slate" shape="square">{{ t('storeSettings.domain.system') }}</app-badge>
        </div>

        @if (customDomains().length > 0) {
          <div class="field">
            <p class="field-label">{{ t('storeSettings.domain.customDomains') }}</p>
            @for (entry of customDomains(); track entry.domain) {
              <div class="info-row domain-row">
                <app-icon name="link" />
                <span class="info-copy">
                  <strong>{{ entry.domain }}</strong>
                  <small>{{ t(copyOf(entry).metaKey) }}</small>
                </span>

                <app-badge [tone]="toneOf(entry)" shape="square">{{ t(copyOf(entry).titleKey) }}</app-badge>

                <button
                  class="ghost-action"
                  type="button"
                  [disabled]="checking() !== null"
                  (click)="verify.emit(entry.domain)"
                >
                  @if (checking() === entry.domain) {
                    <span class="spinner" aria-hidden="true"></span>
                    {{ t('storeSettings.domain.checkingNow') }}
                  } @else {
                    {{ t('storeSettings.domain.check') }}
                  }
                </button>

                <a class="icon-action" [href]="'https://' + entry.domain" target="_blank" rel="noopener"
                   [attr.aria-label]="t('storeSettings.domain.openNamed', {domain: entry.domain})">
                  <app-icon name="externalLink" />
                </a>

                <button
                  class="icon-action danger"
                  type="button"
                  [disabled]="busy()"
                  [attr.aria-label]="t('storeSettings.domain.removeNamed', {domain: entry.domain})"
                  (click)="removed.emit(entry.domain)"
                >
                  <app-icon name="trash" />
                </button>
              </div>
            }
          </div>
        }

        <hr class="divider" />

        <div class="field">
          <label for="custom-domain">{{ t('storeSettings.domain.addCustomDomain') }}</label>
          <div class="domain-row">
            <!--
              The direction is set on the wrapper, not just the input: the scheme and the host are
              one Latin string read left to right, and in an Arabic page an RTL flex row put the
              prefix after the field and reordered it into a mangled //:https.
            -->
            <span class="control host" dir="ltr" [class.invalid]="domainInvalid()">
              <span class="scheme">{{ scheme }}</span>
              <input
                id="custom-domain"
                type="text"
                formControlName="customDomain"
                [attr.inputmode]="'url'"
                [attr.autocapitalize]="'none'"
                [attr.spellcheck]="'false'"
                [placeholder]="exampleDomain"
                (input)="onDomainInput($event)"
              />
            </span>
            <button
              class="ghost-action"
              type="button"
              [disabled]="!canCheckTyped()"
              (click)="verify.emit(typed())"
            >
              @if (checking() === typed()) {
                <span class="spinner" aria-hidden="true"></span>
                {{ t('storeSettings.domain.checkingNow') }}
              } @else {
                {{ t('storeSettings.domain.checkDns') }}
              }
            </button>
          </div>
          <p class="field-hint">{{ t('storeSettings.domain.addHint') }}</p>
          <app-field-error
            [control]="form().controls.customDomain"
            [fallback]="t('storeSettings.domain.fallback')"
          />
        </div>

        @if (typedStatus(); as status) {
          <div class="status" [class]="statusTone(status)">
            <div class="status-head">
              <app-icon [name]="statusCopy(status).icon" />
              <strong>{{ t(statusCopy(status).titleKey) }}</strong>
              <span class="status-meta">{{ t(statusCopy(status).metaKey) }}</span>
            </div>
            <p class="status-body">{{ t(statusCopy(status).bodyKey, {domain: typed()}) }}</p>
          </div>
        }

        @if (record(); as dns) {
          <div class="field">
            <p class="field-label">{{ t('storeSettings.domain.recordToAdd') }}</p>
            <div class="dns">
              <div class="dns-head">
                <span>{{ t('storeSettings.domain.dnsType') }}</span><span>{{ t('storeSettings.domain.dnsName') }}</span><span>{{ t('storeSettings.domain.dnsValue') }}</span>
              </div>
              <div class="dns-row">
                <strong>{{ dns.type }}</strong>
                <!--
                  Monospace suits a hostname and does not suit Arabic — the fallback face breaks the
                  joins and "نطاقك" comes out as loose letters. The placeholder is prose, so it opts
                  out of the row's face; a real hostname keeps it.
                -->
                <span [class.awaiting]="!typed()">{{ dns.name }}</span>
                <app-copy-field [value]="dns.value" [label]="t('storeSettings.domain.dnsValue')" />
              </div>
            </div>
            <p class="field-hint dns-hint">
              <app-icon name="questionCircle" />
              {{ t('storeSettings.domain.dnsHint') }}
            </p>
          </div>
        } @else {
          <!-- No CNAME target: the pod lookup was refused, so the console will not guess one. -->
          <p class="field-hint">{{ t('storeSettings.domain.noTarget') }}</p>
        }
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './domain-section.css'],
})
export class DomainSection {
  // A URL scheme and an example host: neither is language-dependent text, so neither is translated.
  protected readonly scheme = 'https://';

  protected readonly exampleDomain = 'shop.example.com';

  readonly form = input.required<DomainForm>();
  readonly subdomain = input.required<string>();
  readonly customDomains = input.required<readonly StoreDomain[]>();
  /** Every domain that has been looked up, and what the lookup found. Unlisted means unchecked. */
  readonly status = input.required<ReadonlyMap<string, DomainStatus>>();
  readonly checking = input.required<string | null>();
  readonly record = input.required<DnsRecord | null>();
  readonly busy = input(false);

  readonly verify = output<string>();
  readonly removed = output<string>();

  protected toneOf(entry: StoreDomain) {
    return DOMAIN_STATUS_TONE[this.statusOf(entry.domain)];
  }

  protected copyOf(entry: StoreDomain) {
    return DOMAIN_STATUS_COPY[this.statusOf(entry.domain)];
  }

  protected statusTone(status: DomainStatus) {
    return DOMAIN_STATUS_TONE[status];
  }

  protected statusCopy(status: DomainStatus) {
    return DOMAIN_STATUS_COPY[status];
  }

  /**
   * Keeps the field to a hostname.
   *
   * Operators paste out of the address bar, and `https://shop.example.com/` is not something a CNAME
   * can be named after. Normalising as they type means the DNS record below always shows the host it
   * will actually be for, rather than showing a URL and rejecting it a moment later.
   */
  protected onDomainInput(event: Event): void {
    const control = this.form().controls.customDomain;
    const cleaned = bareHostname((event.target as HTMLInputElement).value);
    if (cleaned !== control.value) {
      control.setValue(cleaned);
      control.markAsDirty();
    }
  }

  /** A lookup is worth making only for something that could be a hostname. */
  protected canCheckTyped(): boolean {
    return this.checking() === null && this.typed().length > 0 && this.form().controls.customDomain.valid;
  }

  /**
   * What is in the add field.
   *
   * A method rather than a computed: `value` is not a signal, so a computed would latch onto whatever
   * the control held on first render — the same reason `app-field-error` reads its control per pass.
   */
  protected typed(): string {
    return this.form().controls.customDomain.value.trim();
  }

  /** The verdict on the domain being typed, if it has been looked up. Nothing to say before that. */
  protected typedStatus(): DomainStatus | null {
    const domain = this.typed();
    return domain ? (this.status().get(domain) ?? null) : null;
  }

  protected domainInvalid(): boolean {
    const control = this.form().controls.customDomain;
    return control.invalid && (control.dirty || control.touched);
  }

  private statusOf(domain: string): DomainStatus {
    return this.status().get(domain) ?? 'unverified';
  }
}
