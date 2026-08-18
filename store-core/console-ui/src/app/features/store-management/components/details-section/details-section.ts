import {Component, computed, input, output, signal} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

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
            <label for="support-phone">{{ t('storeSettings.details.supportPhone') }}</label>
            <span class="control">
              <app-icon name="phone" />
              <input id="support-phone" type="tel" formControlName="supportPhone" dir="ltr" />
            </span>
          </div>

          <div class="field">
            <label for="store-currency">
              {{ t('storeSettings.details.currency') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <input id="store-currency" class="control" type="text" formControlName="currency" dir="ltr" required />
            <p class="field-hint">{{ t('storeSettings.details.currencyHint') }}</p>
            <app-field-error
              [control]="form().controls.currency"
              [fallback]="t('storeSettings.details.currencyFallback')"
            />
          </div>

          <div class="field">
            <label for="store-language">
              {{ t('storeSettings.details.language') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <select id="store-language" class="control" formControlName="language" required>
              @for (code of languageOptions(); track code) {
                <option [value]="code">{{ code }}</option>
              }
            </select>
            <app-field-error
              [control]="form().controls.language"
              [fallback]="t('storeSettings.details.languageFallback')"
            />
          </div>

          <div class="field">
            <label for="store-country">{{ t('storeSettings.details.country') }}</label>
            <input id="store-country" class="control" type="text" formControlName="country" dir="ltr" />
            <p class="field-hint">{{ t('storeSettings.details.countryHint') }}</p>
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

  protected readonly descriptionMax = SHORT_DESCRIPTION_MAX;
  protected readonly confirming = signal(false);

  /** `WeightUnit` and `MeasureUnit`. The server's `MeasureUnit` also lists the weight values,
   * which is its own muddle; only the two that measure a distance are offered here. */
  protected readonly weightUnits = ['KG', 'LB'] as const;
  protected readonly dimensionUnits = ['CM', 'IN'] as const;

  /**
   * The languages the select offers.
   *
   * The store's own set, with whatever it is currently on folded in — a store set to a language
   * that has since left `supportedLanguages` would otherwise show a select that silently disagrees
   * with the value bound to it.
   */
  protected readonly languageOptions = computed(() => {
    const offered = this.choices().languages;
    const current = this.form().controls.language.value;
    return current && !offered.includes(current) ? [current, ...offered] : offered;
  });

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
