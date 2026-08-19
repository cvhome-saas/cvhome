import {Component, computed, inject, input, output, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule} from '@angular/forms';
import {switchMap} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {ReferenceDataService, STOREFRONT_LANGUAGES} from '@core/reference/reference-data.service';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {Toggle} from '@shared/ui/toggle/toggle';
import {SHORT_DESCRIPTION_MAX, type SettingsChoices} from '@models/store-settings';
import type {DetailsForm} from '../../services/store-settings-form.service';

/**
 * The store's identity — what it is called, how it is reached, where it trades from, and how its
 * storefront is themed.
 *
 * Two kinds of field sit here and the difference is visible. The first block is
 * `PersistableMerchantStore`: it saves. The second is what `Store Management.dc.html` designs and
 * the platform does not record — rendered, disabled, each with the reason under it, because
 * dropping six fields the design asks for would hide the gap rather than report it. The controls
 * are disabled in `StoreSettingsFormService` and `sectionValueOf` reads `value`, which omits
 * disabled controls — so they cannot reach a request body regardless of what this template does.
 */
@Component({
  selector: 'app-details-section',
  imports: [ConfirmDialog, FieldError, Icon, Panel, ReactiveFormsModule, Toggle, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.details.title')"
      [subtitle]="t('storeSettings.details.subtitle')"
      *transloco="let t"
    >
      <div class="section-body" [formGroup]="form()">
        <div class="field-grid">
          <div class="field">
            <label for="store-name">
              {{ t('storeSettings.details.storeName') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <input id="store-name" class="control" type="text" formControlName="name" dir="auto" required />
            <app-field-error
              [control]="form().controls.name"
              [fallback]="t('storeSettings.details.storeNameFallback')"
            />
          </div>

          <div class="field">
            <label for="support-email">
              {{ t('storeSettings.details.supportEmail') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <span class="control">
              <app-icon name="envelope" />
              <input id="support-email" type="email" formControlName="supportEmail" dir="ltr" required />
            </span>
            <app-field-error
              [control]="form().controls.supportEmail"
              [fallback]="t('storeSettings.details.supportEmailFallback')"
            />
          </div>

          <div class="field">
            <label for="support-phone">
              {{ t('storeSettings.details.supportPhone') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <span class="control">
              <app-icon name="phone" />
              <input
                id="support-phone"
                type="tel"
                formControlName="supportPhone"
                dir="ltr"
                autocomplete="tel"
                required
              />
            </span>
            <app-field-error
              [control]="form().controls.supportPhone"
              [fallback]="t('storeSettings.details.supportPhoneFallback')"
            />
          </div>

          <div class="field">
            <label for="store-currency">
              {{ t('storeSettings.details.currency') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <select id="store-currency" class="control" formControlName="currency" dir="ltr" required>
              @for (currency of currencyOptions(); track currency.code) {
                <option [value]="currency.code">{{ currency.label }}</option>
              }
            </select>
            <app-field-error
              [control]="form().controls.currency"
              [fallback]="t('storeSettings.details.currencyFallback')"
            />
          </div>

          <div class="field field-wide">
            <p class="field-label" id="supported-languages-label">
              {{ t('storeSettings.details.supportedLanguages') }}
              <span class="required" aria-hidden="true">*</span>
            </p>
            <div class="check-grid" role="group" aria-labelledby="supported-languages-label">
              @for (language of supportedLanguageOptions(); track language.code) {
                <label class="check">
                  <input
                    type="checkbox"
                    [checked]="isSupported(language.code)"
                    (change)="toggleLanguage(language.code)"
                  />
                  <span>{{ language.label }}</span>
                </label>
              }
            </div>
            <!--
              The platform adds but never removes: the populator calls add() per entry and drops
              nothing, so unticking is accepted and ignored. Said here rather than discovered after
              a reload. TODO(lessons.md): see lessons.md, "Store management — a supported language
              can be added but never removed".
            -->
            <p class="field-hint">{{ t('storeSettings.details.supportedLanguagesNote') }}</p>
            <app-field-error
              [control]="form().controls.supportedLanguages"
              [fallback]="t('storeSettings.details.supportedLanguagesFallback')"
            />
          </div>

          <div class="field">
            <label for="store-language">
              {{ t('storeSettings.details.language') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <select id="store-language" class="control" formControlName="language" required>
              @for (language of defaultLanguageOptions(); track language.code) {
                <option [value]="language.code">{{ language.label }}</option>
              }
            </select>
            <app-field-error
              [control]="form().controls.language"
              [fallback]="t('storeSettings.details.languageFallback')"
            />
            <!-- The pair is wrong rather than either field, so the message sits under the one to change. -->
            @if (defaultLanguageUnsupported()) {
              <p class="cross-field-error" role="alert">{{ t('storeSettings.details.languageNotSupported') }}</p>
            }
          </div>

          <div class="field">
            <label for="store-country">
              {{ t('storeSettings.details.country') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <select id="store-country" class="control" formControlName="country" required>
              @for (country of countryOptions(); track country.code) {
                <option [value]="country.code">{{ country.label }}</option>
              }
            </select>
            <app-field-error
              [control]="form().controls.country"
              [fallback]="t('storeSettings.details.countryFallback')"
            />
          </div>

          <div class="field">
            <label for="store-theme">{{ t('storeSettings.details.theme') }}</label>
            <select id="store-theme" class="control" formControlName="theme">
              @for (name of choices().themes; track name) {
                <option [value]="name">{{ name }}</option>
              }
            </select>
          </div>

          <div class="field">
            <label for="store-color-theme">{{ t('storeSettings.details.colorTheme') }}</label>
            <select id="store-color-theme" class="control" formControlName="colorTheme">
              @for (name of choices().colorThemes; track name) {
                <option [value]="name">{{ name }}</option>
              }
            </select>
          </div>

          <div class="field">
            <label for="store-since">{{ t('storeSettings.details.inBusinessSince') }}</label>
            <input id="store-since" class="control" type="date" formControlName="inBusinessSince" />
          </div>

          <div class="field">
            <label for="store-weight">{{ t('storeSettings.details.weightUnit') }}</label>
            <select id="store-weight" class="control" formControlName="weightUnit">
              @for (unit of weightUnits; track unit) {
                <option [value]="unit">{{ t('storeSettings.unit.' + unit) }}</option>
              }
            </select>
          </div>

          <div class="field">
            <label for="store-dimension">{{ t('storeSettings.details.dimensionUnit') }}</label>
            <select id="store-dimension" class="control" formControlName="dimensionUnit">
              @for (unit of dimensionUnits; track unit) {
                <option [value]="unit">{{ t('storeSettings.unit.' + unit) }}</option>
              }
            </select>
          </div>

          <div class="field field-wide">
            <label for="store-address">{{ t('storeSettings.details.address') }}</label>
            <span class="control">
              <app-icon name="mapPin" />
              <input id="store-address" type="text" formControlName="addressLine" dir="auto" />
            </span>
          </div>

          <div class="field">
            <label for="store-city">{{ t('storeSettings.details.city') }}</label>
            <input id="store-city" class="control" type="text" formControlName="city" dir="auto" />
          </div>

          <div class="field">
            <label for="store-state">{{ t('storeSettings.details.stateProvince') }}</label>
            <input id="store-state" class="control" type="text" formControlName="stateProvince" dir="auto" />
          </div>

          <div class="field">
            <label for="store-postal">{{ t('storeSettings.details.postalCode') }}</label>
            <input id="store-postal" class="control" type="text" formControlName="postalCode" dir="ltr" />
          </div>
        </div>

        <hr class="divider" />

        <div class="storefront-flags">
          <p class="field-label">{{ t('storeSettings.details.storefrontBehaviour') }}</p>

          <div class="switch-row">
            <app-toggle
              [label]="t('storeSettings.details.requireLogin')"
              [description]="t('storeSettings.details.requireLoginDescription')"
              [checked]="form().controls.requireLoginForOrderPlacement.value"
              (checkedChange)="setFlag('requireLoginForOrderPlacement', $event)"
            />
          </div>

          <div class="switch-row">
            <app-toggle
              [label]="t('storeSettings.details.useCache')"
              [description]="t('storeSettings.details.useCacheDescription')"
              [checked]="form().controls.useCache.value"
              (checkedChange)="setFlag('useCache', $event)"
            />
          </div>
        </div>

        <hr class="divider" />

        <!--
          Designed, unrecorded. Grouped rather than scattered through the grid above, so it reads
          as one honest statement about the platform instead of six broken-looking controls.
        -->
        <div class="unbacked">
          <p class="field-label">{{ t('storeSettings.details.notRecorded') }}</p>
          <p class="unbacked-note">{{ t('storeSettings.details.notRecordedNote') }}</p>

          <div class="field-grid">
            <div class="field">
              <label for="legal-name">{{ t('storeSettings.details.legalName') }}</label>
              <input id="legal-name" class="control" type="text" formControlName="legalName" />
            </div>

            <div class="field">
              <label for="store-slug">{{ t('storeSettings.details.slug') }}</label>
              <span class="control">
                <app-icon name="link" />
                <input id="store-slug" type="text" formControlName="slug" />
              </span>
            </div>

            <div class="field">
              <label for="store-category">{{ t('storeSettings.details.category') }}</label>
              <input id="store-category" class="control" type="text" formControlName="category" />
            </div>

            <div class="field">
              <label for="store-timezone">{{ t('storeSettings.details.timezone') }}</label>
              <input id="store-timezone" class="control" type="text" formControlName="timezone" />
            </div>

            <div class="field">
              <label for="store-tax">{{ t('storeSettings.details.taxNumber') }}</label>
              <input id="store-tax" class="control" type="text" formControlName="taxNumber" />
            </div>

            <div class="field field-wide">
              <label for="store-blurb">{{ t('storeSettings.details.shortDescription') }}</label>
              <input id="store-blurb" class="control" type="text" formControlName="shortDescription" />
              <p class="field-hint">{{ t('storeSettings.details.upToCharacters', {max: descriptionMax}) }}</p>
            </div>
          </div>

          <div class="switch-row">
            <app-toggle
              [label]="t('storeSettings.details.published')"
              [description]="t('storeSettings.details.publishedDescription')"
              [checked]="false"
              [disabled]="true"
            />
          </div>

          <div class="switch-row">
            <app-toggle
              [label]="t('storeSettings.details.maintenanceMode')"
              [description]="t('storeSettings.details.maintenanceModeDescription')"
              [checked]="false"
              [disabled]="true"
            />
          </div>
        </div>

        <div class="danger-zone">
          <app-icon name="alertCircle" />
          <div class="danger-copy">
            <strong>{{ t('storeSettings.details.deleteStore') }}</strong>
            <span>{{ t('storeSettings.details.deleteStoreWarning') }}</span>
          </div>
          <button
            class="danger-action"
            type="button"
            [disabled]="isDeleting()"
            (click)="confirming.set(true)"
          >
            {{ t('storeSettings.details.deleteStore') }}
          </button>
        </div>
      </div>

      <app-confirm-dialog
        [open]="confirming()"
        [title]="t('storeSettings.details.deleteStore')"
        [message]="t('storeSettings.details.deleteStoreConfirm')"
        [confirmationText]="storeName()"
        [confirmationLabel]="t('storeSettings.details.deleteStoreType')"
        [confirmLabel]="t('storeSettings.details.deleteStoreAccept')"
        [cancelLabel]="t('actions.cancel')"
        (confirmed)="onConfirmed()"
        (dismissed)="confirming.set(false)"
      />
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './details-section.css'],
})
export class DetailsSection {
  readonly form = input.required<DetailsForm>();
  readonly choices = input.required<SettingsChoices>();
  readonly storeName = input.required<string>();
  readonly isDeleting = input(false);

  readonly deleteRequested = output<void>();

  private readonly reference = inject(ReferenceDataService);

  protected readonly descriptionMax = SHORT_DESCRIPTION_MAX;
  protected readonly confirming = signal(false);

  /**
   * The details form's value, as a signal.
   *
   * A `FormGroup` is not reactive, so a `computed` reading `.value` directly would compute once
   * and never again — the country select would keep offering options for the store that loaded
   * first. `form.events` fires on value, status, touched and pristine changes alike, which is the
   * dependency these computeds actually need.
   */
  private readonly formEvent = toSignal(
    toObservable(this.form).pipe(switchMap((form) => form.events)),
    {initialValue: null},
  );

  private readonly formValue = computed(() => {
    this.formEvent();
    return this.form().getRawValue();
  });

  /** `WeightUnit` and `MeasureUnit`. The server's `MeasureUnit` also lists the weight values,
   * which is its own muddle; only the two that measure a distance are offered here. */
  protected readonly weightUnits = ['KG', 'LB'] as const;
  protected readonly dimensionUnits = ['CM', 'IN'] as const;

  /**
   * Every country and every currency, named in the reader's language.
   *
   * Both come from `Intl` rather than from the platform, which serves neither list — the store's
   * stored code is folded in so the select cannot silently disagree with the value bound to it.
   */
  protected readonly countryOptions = computed(() =>
    this.reference.withCurrent(this.reference.countries(), this.formValue().country),
  );

  protected readonly currencyOptions = computed(() =>
    this.reference.withCurrent(this.reference.currencies(), this.formValue().currency),
  );

  /**
   * The languages a storefront may be published in.
   *
   * `STOREFRONT_LANGUAGES` is the platform's set, and there is no endpoint that lists it. Whatever
   * the store already has is folded in on top: a store carrying a language outside the constant
   * must still be able to see it ticked, and must not lose it by saving.
   */
  protected readonly supportedLanguageOptions = computed(() => {
    const stored = [...this.choices().languages, ...this.formValue().supportedLanguages];
    const codes = [...new Set([...STOREFRONT_LANGUAGES, ...stored])];
    const collator = new Intl.Collator(this.reference.activeLang());
    return codes
      .map((code) => ({code, label: this.reference.languageName(code)}))
      .sort((left, right) => collator.compare(left.label, right.label));
  });

  /**
   * What the default-language select offers: the languages actually ticked above.
   *
   * A default the storefront does not publish is not a choice, so unticking a language removes it
   * from here. The current value is kept visible when that happens — `defaultLanguageUnsupported`
   * is what says it is wrong, rather than the select quietly showing a different language than the
   * one the form holds.
   */
  protected readonly defaultLanguageOptions = computed(() => {
    const value = this.formValue();
    const ticked = this.supportedLanguageOptions().filter((language) =>
      value.supportedLanguages.includes(language.code),
    );
    return this.reference.withCurrent(ticked, value.language);
  });

  /**
   * Whether to say that the default is not one of the supported languages.
   *
   * Held back until one of the two fields has been touched, the same rule `FieldError` follows:
   * a store that arrives in this state should not be shouted at before the operator has done
   * anything, but the moment they change either field the pair has to explain itself.
   */
  protected readonly defaultLanguageUnsupported = computed(() => {
    this.formEvent();
    const form = this.form();
    return (
      form.hasError('defaultLanguageNotSupported') &&
      (form.controls.language.dirty ||
        form.controls.language.touched ||
        form.controls.supportedLanguages.dirty)
    );
  });

  protected isSupported(code: string): boolean {
    return this.formValue().supportedLanguages.includes(code);
  }

  /**
   * Ticks or unticks one language.
   *
   * Rebuilt rather than mutated: the control holds a `readonly string[]`, and a form control whose
   * value is edited in place emits nothing, so *Save changes* would never light up. Order follows
   * `supportedLanguageOptions` so the saved list does not churn with the order of clicks.
   */
  protected toggleLanguage(code: string): void {
    const control = this.form().controls.supportedLanguages;
    const chosen = new Set(control.value);
    if (!chosen.delete(code)) {
      chosen.add(code);
    }
    control.setValue(this.supportedLanguageOptions().map((language) => language.code).filter((entry) => chosen.has(entry)));
    control.markAsDirty();
  }

  /**
   * `setValue` leaves the form pristine, and *Save changes* keys off dirty — so a control
   * written by a component rather than by a keystroke has to say that it changed.
   */
  protected setFlag(name: 'requireLoginForOrderPlacement' | 'useCache', value: boolean): void {
    const control = this.form().controls[name];
    control.setValue(value);
    control.markAsDirty();
  }

  protected onConfirmed(): void {
    this.confirming.set(false);
    this.deleteRequested.emit();
  }
}
