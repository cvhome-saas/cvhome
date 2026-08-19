import {Component, input, output} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {CopyField} from '@shared/ui/copy-field/copy-field';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import type {CnameOutcome} from '@api/dns/dns-check.service';
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
                </span>

                <!--
                  A badge only once a lookup has actually run. An allocated domain is not in an
                  unchecked state — the field refuses to add one that does not already point here —
                  so a permanent "not checked" would have been the console doubting its own rule.
                  What is worth saying is what a *re*-check found, because DNS can change afterwards.
                -->
                @if (statusOf(entry.domain); as status) {
                  <app-badge [tone]="statusTone(status)" shape="square">{{ t(statusCopy(status).titleKey) }}</app-badge>
                }

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
            <!--
              The field checks itself as it is typed; this re-runs it, for the operator who has just
              fixed the record at their registrar and does not want to retype the domain to find out.
            -->
            <button
              class="ghost-action"
              type="button"
              [disabled]="!canRecheck()"
              (click)="recheck()"
            >
              @if (pending()) {
                <span class="spinner" aria-hidden="true"></span>
                {{ t('storeSettings.domain.checkingNow') }}
              } @else {
                {{ t('storeSettings.domain.checkAgain') }}
              }
            </button>
          </div>
          <p class="field-hint">{{ t('storeSettings.domain.addHint') }}</p>
          <!--
            The inline error speaks for the shape of the value only. What the DNS check found is the
            status panel's job, below — it has room to name the domain and say what to do about it,
            and one message in two places would be two places to keep in step.
          -->
          @if (form().controls.customDomain.hasError('pattern')) {
            <app-field-error
              [control]="form().controls.customDomain"
              [fallback]="t('storeSettings.domain.fallback')"
            />
          }
          @if (checkUnavailable()) {
            <!--
              The resolver could not be reached, so the domain is neither approved nor refused. Said
              plainly rather than blocking: a network that filters dns.google would otherwise make
              the field impossible to use.
            -->
            <p class="field-warning" role="status">{{ t('storeSettings.domain.checkUnavailable') }}</p>
          }
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
  /** Every *allocated* domain that has been looked up, and what the lookup found. Unlisted means unchecked. */
  readonly status = input.required<ReadonlyMap<string, DomainStatus>>();
  readonly checking = input.required<string | null>();
  /** The last lookup could not be made at all — neither approval nor refusal. */
  readonly checkUnavailable = input(false);
  readonly record = input.required<DnsRecord | null>();
  readonly busy = input(false);

  readonly verify = output<string>();
  readonly removed = output<string>();

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

  /** A re-check is worth offering only for something that could be a hostname and is not mid-flight. */
  protected canRecheck(): boolean {
    const control = this.form().controls.customDomain;
    return this.typed().length > 0 && !control.pending && !control.hasError('pattern');
  }

  /**
   * Runs the field's own check again.
   *
   * `updateValueAndValidity()` re-runs the async validator against the value already in the control,
   * which is the point: the domain has not changed, the record at the registrar has.
   */
  protected recheck(): void {
    this.form().controls.customDomain.updateValueAndValidity();
  }

  protected pending(): boolean {
    return this.form().controls.customDomain.pending;
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

  /**
   * The verdict on the domain being typed, read off the control rather than kept beside it.
   *
   * The field validates itself, so its state *is* the verdict: pending is a lookup in flight, a
   * `dnsNotPointing` error carries what the resolver actually found, and a valid, non-empty value
   * that had something to be checked against is a domain that points here. Deriving the panel from
   * the control is what keeps the two from disagreeing — a separate status could say "verified"
   * under a field the form considers invalid.
   */
  protected typedStatus(): DomainStatus | null {
    const control = this.form().controls.customDomain;
    if (!this.typed() || control.hasError('pattern')) {
      return null;
    }
    if (control.pending) {
      return 'checking';
    }
    const failure = control.getError('dnsNotPointing') as {outcome: CnameOutcome} | undefined;
    if (failure) {
      return failure.outcome === 'points-elsewhere' ? 'failed' : 'waiting';
    }
    // Valid, but nothing was compared: no pod target, or the resolver could not be reached.
    if (!this.record() || this.checkUnavailable()) {
      return null;
    }
    return 'verified';
  }

  protected domainInvalid(): boolean {
    const control = this.form().controls.customDomain;
    return control.invalid && (control.dirty || control.touched);
  }

  /** What the last lookup on this domain found, or nothing if none has run. */
  protected statusOf(domain: string): DomainStatus | null {
    return this.status().get(domain) ?? null;
  }
}
