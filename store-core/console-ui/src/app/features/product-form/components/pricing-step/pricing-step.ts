import {Component, computed, inject, input} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {DIMENSION_UNITS, WEIGHT_UNITS} from '@models/products';
import {FieldError} from '@shared/ui/form-field/field-error';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {NumberField} from '@shared/ui/number-field/number-field';
import {Panel} from '@shared/ui/panel/panel';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {Toggle} from '@shared/ui/toggle/toggle';
import type {ProductForm} from '../../services/product-form.service';

/**
 * Step 3 — what it costs and how much there is.
 *
 * **This is the step the design and the platform disagree about most.** Gone, each with an entry in
 * lessons.md: compare-at price, unit cost and the margin derived from it, bulk pricing tiers, tax
 * class, per-product currency, per-location opening quantity, the reorder point, and "allow
 * backorders". None has a field, a table or an endpoint. `productQuantityOrderMin`/`Max` look like
 * a backorder flag and are not — they are per-order purchase limits, and reusing them would be a
 * fixture standing in for a real answer.
 *
 * What is left is what a product actually carries: one price, one quantity, three flags and a
 * shipping box.
 */
@Component({
  selector: 'app-pricing-step',
  imports: [
    FieldError,
    NoticeBar,
    NumberField,
    Panel,
    ReactiveFormsModule,
    Select,
    Toggle,
    TranslocoDirective,
  ],
  templateUrl: './pricing-step.html',
  styleUrls: ['../editor-card.css', './pricing-step.css'],
})
export class PricingStep {
  readonly form = input.required<ProductForm>();
  /** The store's currency, shown beside the price. There is no per-product currency. */
  readonly currency = input<string | null>(null);

  private readonly transloco = inject(TranslocoService);

  /*
   * The server owns both enums, so each label goes through the known-set guard Module 4 established:
   * the arrays are the constants, never a value that arrived on the wire, because an unknown key
   * would take the page down against the strict missing-key handler.
   */
  protected readonly weightUnitOptions = computed<readonly SelectOption[]>(() =>
    this.unitOptions(WEIGHT_UNITS),
  );

  protected readonly dimensionUnitOptions = computed<readonly SelectOption[]>(() =>
    this.unitOptions(DIMENSION_UNITS),
  );

  private unitOptions(units: readonly string[]): readonly SelectOption[] {
    this.transloco.activeLang();
    return units.map((unit) => ({
      value: unit,
      label: this.transloco.translate(`productForm.pricing.unit.${unit}`),
    }));
  }
}
