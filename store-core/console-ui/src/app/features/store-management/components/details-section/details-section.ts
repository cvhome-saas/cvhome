import {Component, computed, inject, input, output, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule} from '@angular/forms';
import {switchMap} from 'rxjs';
import {TranslocoDirective} from '@jsverse/transloco';

import {dateKey} from '@core/i18n/calendar';
import {ReferenceDataService, STOREFRONT_LANGUAGES} from '@core/reference/reference-data.service';
import {Checkbox} from '@shared/ui/checkbox/checkbox';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {DatePicker} from '@shared/ui/date-picker/date-picker';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TextField} from '@shared/ui/text-field/text-field';
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
  imports: [
    Checkbox,
    ConfirmDialog,
    DatePicker,
    FormField,
    Icon,
    Panel,
    ReactiveFormsModule,
    Select,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  template: `
    <app-panel
      [title]="t('storeSettings.details.title')"
      [subtitle]="t('storeSettings.details.subtitle')"
      *transloco="let t"
    >
      <div class="section-body" [formGroup]="form()">
        <div class="field-grid">
          <app-form-field
            [label]="t('storeSettings.details.storeName')"
            [control]="form().controls.name"
            controlId="store-name"
            required
            [fallback]="t('storeSettings.details.storeNameFallback')"
          >
            <app-text-field id="store-name" formControlName="name" />
          </app-form-field>

          <app-form-field
            [label]="t('storeSettings.details.supportEmail')"
            [control]="form().controls.supportEmail"
            required
            [fallback]="t('storeSettings.details.supportEmailFallback')"
          >
            <app-text-field type="email" formControlName="supportEmail" icon="envelope" latin autocomplete="email" />
          </app-form-field>

          <app-form-field
            [label]="t('storeSettings.details.supportPhone')"
            [control]="form().controls.supportPhone"
            controlId="support-phone"
            required
            [fallback]="t('storeSettings.details.supportPhoneFallback')"
          >
            <app-text-field
              id="support-phone"
              type="tel"
              formControlName="supportPhone"
              icon="phone"
              latin
              autocomplete="tel"
            />
          </app-form-field>

          <app-form-field
            [label]="t('storeSettings.details.currency')"
            [control]="form().controls.currency"
            required
            [fallback]="t('storeSettings.details.currencyFallback')"
          >
            <app-select formControlName="currency" [options]="currencyChoices()" />
          </app-form-field>

          <app-form-field
            wide
            [label]="t('storeSettings.details.supportedLanguages')"
            [control]="form().controls.supportedLanguages"
            required
            [fallback]="t('storeSettings.details.supportedLanguagesFallback')"
            [hint]="t('storeSettings.details.supportedLanguagesNote')"
            controlId="supported-languages"
          >
            <!--
              The hint above is not decoration: the platform adds but never removes — the populator
              calls add() per entry and drops nothing, so unticking is accepted and ignored. Said
              before the operator spends a save finding out. TODO(lessons.md): see lessons.md,
              "Store management — a supported language can be added but never removed".
            -->
            <div class="check-grid" role="group" [attr.aria-label]="t('storeSettings.details.supportedLanguages')">
              @for (language of supportedLanguageOptions(); track language.code) {
                <app-checkbox
                  [label]="language.label"
                  [checked]="isSupported(language.code)"
                  (checkedChange)="toggleLanguage(language.code)"
                />
              }
            </div>
          </app-form-field>

          <app-form-field
            [label]="t('storeSettings.details.language')"
            [control]="form().controls.language"
            required
            [fallback]="t('storeSettings.details.languageFallback')"
          >
            <app-select formControlName="language" [options]="defaultLanguageChoices()" />
          </app-form-field>

          <!-- The pair is wrong rather than either field, so the message sits under the one to change. -->
          @if (defaultLanguageUnsupported()) {
            <p class="cross-field-error" role="alert">{{ t('storeSettings.details.languageNotSupported') }}</p>
          }

          <app-form-field
            [label]="t('storeSettings.details.country')"
            [control]="form().controls.country"
            required
            [fallback]="t('storeSettings.details.countryFallback')"
          >
            <app-select formControlName="country" [options]="countryChoices()" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.theme')">
            <app-select formControlName="theme" [options]="themeChoices()" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.colorTheme')">
            <app-select formControlName="colorTheme" [options]="colorThemeChoices(t)" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.inBusinessSince')">
            <app-date-picker
              formControlName="inBusinessSince"
              [max]="today"
              [placeholder]="t('storeSettings.details.inBusinessSincePlaceholder')"
            />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.weightUnit')">
            <app-select formControlName="weightUnit" [options]="weightUnitChoices(t)" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.dimensionUnit')">
            <app-select formControlName="dimensionUnit" [options]="dimensionUnitChoices(t)" />
          </app-form-field>

          <app-form-field wide [label]="t('storeSettings.details.address')">
            <app-text-field formControlName="addressLine" icon="mapPin" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.city')">
            <app-text-field formControlName="city" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.stateProvince')">
            <app-text-field formControlName="stateProvince" />
          </app-form-field>

          <app-form-field [label]="t('storeSettings.details.postalCode')">
            <app-text-field formControlName="postalCode" latin />
          </app-form-field>
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
            <app-form-field [label]="t('storeSettings.details.legalName')">
              <app-text-field formControlName="legalName" />
            </app-form-field>

            <app-form-field [label]="t('storeSettings.details.slug')">
              <app-text-field formControlName="slug" icon="link" latin />
            </app-form-field>

            <app-form-field [label]="t('storeSettings.details.category')">
              <app-text-field formControlName="category" />
            </app-form-field>

            <app-form-field [label]="t('storeSettings.details.timezone')">
              <app-text-field formControlName="timezone" latin />
            </app-form-field>

            <app-form-field [label]="t('storeSettings.details.taxNumber')">
              <app-text-field formControlName="taxNumber" latin />
            </app-form-field>

            <app-form-field
              wide
              [label]="t('storeSettings.details.shortDescription')"
              [hint]="t('storeSettings.details.upToCharacters', {max: descriptionMax})"
            >
              <app-text-field formControlName="shortDescription" [maxLength]="descriptionMax" />
            </app-form-field>
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
        [cancelLabel]="t('shared.actions.cancel')"
        (confirmed)="onConfirmed()"
        (dismissed)="confirming.set(false)"
      />
    </app-panel>
  `,
  styleUrls: ['../../../../shared/styles/field.css', '../settings-card.css', './details-section.css'],
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

  /** Today, as the date picker's upper bound — a store cannot have traded since tomorrow. */
  protected readonly today = dateKey(new Date());
  protected readonly dimensionUnits = ['CM', 'IN'] as const;

  /**
   * The reference lists, as `app-select` options.
   *
   * `SelectOption` rather than the raw arrays because the native `<select>`s these replaced built
   * their own `<option>` elements — and did so with `[value]`, which is the binding that loses its
   * value when the options arrive after it. `formControlName` on `app-select` re-applies as each
   * option appears, which is the fix lessons.md records for exactly this.
   */
  protected readonly countryChoices = computed<readonly SelectOption[]>(() =>
    this.countryOptions().map((option) => ({value: option.code, label: option.label})),
  );

  protected readonly currencyChoices = computed<readonly SelectOption[]>(() =>
    this.currencyOptions().map((option) => ({value: option.code, label: option.label})),
  );

  protected readonly defaultLanguageChoices = computed<readonly SelectOption[]>(() =>
    this.defaultLanguageOptions().map((option) => ({value: option.code, label: option.label})),
  );

  /** A theme is named by the server and has no translation; the code is the label. */
  protected readonly themeChoices = computed<readonly SelectOption[]>(() =>
    this.choices().themes.map((name) => ({value: name, label: name})),
  );

  /** `DEFAULT` is not a palette but "the storefront theme's own colours", so it gets words. */
  protected colorThemeChoices(t: (key: string) => string): readonly SelectOption[] {
    return this.choices().colorThemes.map((name) => ({
      value: name,
      label: name === 'DEFAULT' ? t('storeSettings.details.colorThemeDefault') : name,
    }));
  }

  protected weightUnitChoices(t: (key: string) => string): readonly SelectOption[] {
    return this.weightUnits.map((unit) => ({value: unit, label: t('storeSettings.unit.' + unit)}));
  }

  protected dimensionUnitChoices(t: (key: string) => string): readonly SelectOption[] {
    return this.dimensionUnits.map((unit) => ({value: unit, label: t('storeSettings.unit.' + unit)}));
  }

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
