import {Component, inject, input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {FieldError} from '@shared/ui/form-field/field-error';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {ToastService} from '@shared/ui/toast/toast';
import {Toggle} from '@shared/ui/toggle/toggle';
import {SHORT_DESCRIPTION_MAX} from '@models/store-settings';
import type {DetailsForm} from '../../services/store-settings-form.service';

/**
 * Identity, contact and locale settings — the fields off `MerchantStoreDetails` and the store's
 * address.
 *
 * The two switches are part of the same form as the text fields, so publishing a store and
 * renaming it are one save rather than two.
 */
@Component({
  selector: 'app-details-section',
  imports: [FieldError, Icon, Panel, ReactiveFormsModule, Toggle, TranslocoDirective],
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
            <input id="store-name" class="control" type="text" formControlName="name" required />
            <app-field-error
              [control]="form().controls.name"
              [fallback]="t('storeSettings.details.storeNameFallback')"
            />
          </div>

          <div class="field">
            <label for="legal-name">
              {{ t('storeSettings.details.legalName') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <input id="legal-name" class="control" type="text" formControlName="legalName" required />
            <app-field-error
              [control]="form().controls.legalName"
              [fallback]="t('storeSettings.details.legalNameFallback')"
            />
          </div>

          <div class="field">
            <label for="store-slug">
              {{ t('storeSettings.details.slug') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <span class="control">
              <app-icon name="link" />
              <input id="store-slug" type="text" formControlName="slug" required />
            </span>
            <p class="field-hint">{{ t('storeSettings.details.slugHint') }}</p>
            <app-field-error
              [control]="form().controls.slug"
              [fallback]="t('storeSettings.details.slugFallback')"
            />
          </div>

          <div class="field">
            <label for="store-category">{{ t('storeSettings.details.category') }}</label>
            <input id="store-category" class="control" type="text" formControlName="category" />
          </div>

          <div class="field">
            <label for="support-email">
              {{ t('storeSettings.details.supportEmail') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <span class="control">
              <app-icon name="envelope" />
              <input id="support-email" type="email" formControlName="supportEmail" required />
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
              <input id="support-phone" type="tel" formControlName="supportPhone" />
            </span>
          </div>

          <div class="field">
            <label for="store-currency">
              {{ t('storeSettings.details.currency') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <input id="store-currency" class="control" type="text" formControlName="currency" required />
            <app-field-error
              [control]="form().controls.currency"
              [fallback]="t('storeSettings.details.currencyFallback')"
            />
          </div>

          <div class="field">
            <label for="store-language">
              {{ t('storeSettings.details.language') }} <span class="required" aria-hidden="true">*</span>
            </label>
            <input id="store-language" class="control" type="text" formControlName="language" required />
            <app-field-error
              [control]="form().controls.language"
              [fallback]="t('storeSettings.details.languageFallback')"
            />
          </div>

          <div class="field">
            <label for="store-timezone">{{ t('storeSettings.details.timezone') }}</label>
            <input id="store-timezone" class="control" type="text" formControlName="timezone" />
          </div>

          <div class="field">
            <label for="store-tax">{{ t('storeSettings.details.taxNumber') }}</label>
            <input id="store-tax" class="control" type="text" formControlName="taxNumber" />
            <p class="field-hint">{{ t('storeSettings.details.taxHint') }}</p>
          </div>

          <div class="field field-wide">
            <label for="store-address">{{ t('storeSettings.details.address') }}</label>
            <span class="control">
              <app-icon name="mapPin" />
              <input id="store-address" type="text" formControlName="address" />
            </span>
          </div>

          <div class="field field-wide">
            <label for="store-blurb">{{ t('storeSettings.details.shortDescription') }}</label>
            <input id="store-blurb" class="control" type="text" formControlName="shortDescription" />
            <p class="field-foot">
              <span>{{ t('storeSettings.details.upToCharacters', {max: descriptionMax}) }}</span>
              <span class="counter" [class.over]="blurbLength() > descriptionMax">
                {{ blurbLength() }}/{{ descriptionMax }}
              </span>
            </p>
            <app-field-error
              [control]="form().controls.shortDescription"
              [fallback]="t('storeSettings.details.shortDescriptionFallback', {max: descriptionMax})"
            />
          </div>
        </div>

        <hr class="divider" />

        <div class="visibility">
          <p class="field-label">{{ t('storeSettings.details.visibility') }}</p>

          <div class="switch-row" [class.on]="form().controls.published.value">
            <app-toggle
              [label]="t('storeSettings.details.published')"
              [description]="t('storeSettings.details.publishedDescription')"
              [checked]="form().controls.published.value"
              (checkedChange)="setFlag(form().controls.published, $event)"
            />
          </div>

          <div class="switch-row">
            <app-toggle
              [label]="t('storeSettings.details.maintenanceMode')"
              [description]="t('storeSettings.details.maintenanceModeDescription')"
              [checked]="form().controls.maintenanceMode.value"
              (checkedChange)="setFlag(form().controls.maintenanceMode, $event)"
            />
          </div>
        </div>

        <div class="danger-zone">
          <app-icon name="alertCircle" />
          <div class="danger-copy">
            <strong>{{ t('storeSettings.details.deleteStore') }}</strong>
            <span>{{ t('storeSettings.details.deleteStoreWarning') }}</span>
          </div>
          <button class="danger-action" type="button" (click)="deleteStore()">{{ t('storeSettings.details.deleteStore') }}</button>
        </div>
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './details-section.css'],
})
export class DetailsSection {
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly form = input.required<DetailsForm>();

  protected readonly descriptionMax = SHORT_DESCRIPTION_MAX;

  protected blurbLength(): number {
    return this.form().controls.shortDescription.value.length;
  }

  /**
   * `setValue` leaves the form pristine, and *Save changes* keys off dirty — so a control
   * written by a component rather than by a keystroke has to say that it changed.
   */
  protected setFlag(control: FormControl<boolean>, value: boolean): void {
    control.setValue(value);
    control.markAsDirty();
  }

  protected deleteStore(): void {
    this.toast.info(this.transloco.translate('storeSettings.details.deleteNotAvailable'));
  }
}
