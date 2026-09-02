import {Component, computed, inject, input, signal} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {MAX_VARIANTS, MAX_VARIANT_OPTIONS} from '@models/products';
import {Icon, NoticeBar, NumberField, Panel, Select, type SelectOption, TextField, Toggle} from '@cvhome-saas/ui-kit/ui';
import {ProductFormFacade} from '../../facades/product-form.facade';

/**
 * Step 4 — what this product varies by, and the matrix of combinations it sells.
 *
 * Available to every saved product, Shopify-style: pick options from the store vocabulary (defined
 * on `/catalogue/options`), and the cartesian product generates immediately — every combination a
 * row, each with its own sku, price, stock and availability. Removing a row stops selling that
 * combination; the add-combination controls bring one back. Exactly one row is the default: the
 * card/list price and the PDP preselection.
 *
 * **This step saves itself.** The axes and the matrix go to catalog as one atomic PUT, then prices
 * and stock go to inventory as one bulk upsert (plus cleanup of retired skus) — a different
 * transaction against a different pair of services than the header's Save draft, which is why the
 * button lives here. A save whose inventory half failed is shown as a named, retryable state,
 * never a silent half-save.
 */
@Component({
  selector: 'app-variants-step',
  imports: [
    Icon,
    NoticeBar,
    NumberField,
    Panel,
    Select,
    TextField,
    Toggle,
    TranslocoDirective,
  ],
  templateUrl: './variants-step.html',
  styleUrls: ['../../../../shared/styles/field.css', '../editor-card.css', './variants-step.css'],
})
export class VariantsStep {
  readonly saved = input.required<boolean>();
  /** The store's currency, shown beside each price. There is no per-product currency. */
  readonly currency = input<string | null>(null);

  protected readonly facade = inject(ProductFormFacade);
  private readonly transloco = inject(TranslocoService);

  protected readonly maxOptions = MAX_VARIANT_OPTIONS;
  protected readonly maxVariants = MAX_VARIANTS;

  /** The vocabulary entries not yet chosen as axes — what the "add option" select offers. */
  protected readonly availableOptions = computed<readonly SelectOption[]>(() => {
    const chosen = new Set(this.facade.variantAxes().map((axis) => axis.id));
    return this.facade
      .vocabulary()
      .filter((option) => !chosen.has(option.id))
      .map((option) => ({
        value: String(option.id),
        label: option.name,
        detail: option.code,
        // Offered but refused with a reason, rather than silently missing from the list.
        disabled: option.values.length === 0,
      }));
  });

  protected onAddAxis(value: string): void {
    const id = Number(value);
    if (Number.isFinite(id) && id > 0) {
      this.facade.addVariantAxis(id);
    }
    // The select is a command, not a state: it snaps back to the placeholder either way.
    this.axisPick.set('');
  }

  /** Bound to the add-option select so it can be reset after each pick. */
  protected readonly axisPick = signal('');

  /* ----------------------------------------------------------- add a combination ---- */

  /** One picked value id per axis, aligned with `variantAxes()`. Empty string = not picked yet. */
  protected readonly combinationPicks = signal<readonly string[]>([]);

  protected onCombinationPick(index: number, value: string): void {
    const axes = this.facade.variantAxes().length;
    const current = [...this.combinationPicks()];
    while (current.length < axes) {
      current.push('');
    }
    current[index] = value;
    this.combinationPicks.set(current.slice(0, axes));
  }

  protected combinationPick(index: number): string {
    return this.combinationPicks()[index] ?? '';
  }

  protected canAddCombination(): boolean {
    const axes = this.facade.variantAxes().length;
    if (axes === 0) {
      return false;
    }
    const picks = this.combinationPicks();
    return picks.length >= axes && picks.slice(0, axes).every((pick) => pick !== '');
  }

  protected addCombination(): void {
    if (!this.canAddCombination()) {
      return;
    }
    this.facade.addVariantCombination(this.combinationPicks().map((pick) => Number(pick)));
    this.combinationPicks.set([]);
  }

  protected valuesOf(axisIndex: number): readonly SelectOption[] {
    const axis = this.facade.variantAxes()[axisIndex];
    return (axis?.values ?? []).map((value) => ({value: String(value.id), label: value.name}));
  }

  /** The values summary an axis chip shows — "Red · Blue", not a bare count. */
  protected axisValues(axisIndex: number): string {
    const axis = this.facade.variantAxes()[axisIndex];
    return (axis?.values ?? []).map((value) => value.name).join(' · ');
  }

  protected rowLabel(labels: readonly string[]): string {
    return labels.join(' / ');
  }

  protected skuInvalid(sku: string): boolean {
    return !/^[A-Za-z0-9_-]+$/.test(sku);
  }

  protected countLine(): string {
    this.transloco.activeLang();
    return this.transloco.translate('productForm.variants.count', {
      count: this.facade.variantRows().length,
      max: this.maxVariants,
    });
  }
}
