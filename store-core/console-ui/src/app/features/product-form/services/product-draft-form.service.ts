import {Injectable, inject} from '@angular/core';
import {
  FormBuilder,
  Validators,
  type AsyncValidatorFn,
  type FormArray,
  type FormControl,
  type FormGroup,
} from '@angular/forms';

import type {ProductDraft} from '@models/products';
import {
  HIGHLIGHTS_MAX,
  KEYWORDS_MAX,
  META_DESCRIPTION_MAX,
  NAME_MAX,
  SKU_MAX,
  SLUG_MAX,
  TITLE_MAX,
  UNIQUENESS_DEBOUNCE_MS,
  type LocalisedCopy,
} from '@models/taxonomy';
import {ProductFormApi} from './product-form.api.service';
import {uniqueAsync} from '@cvhome-saas/ui-kit/forms';

/** One language's copy on the product form. */
export type ProductCopyForm = FormGroup<{
  language: FormControl<string>;
  name: FormControl<string>;
  friendlyUrl: FormControl<string>;
  highlights: FormControl<string>;
  description: FormControl<string>;
  title: FormControl<string>;
  metaDescription: FormControl<string>;
  keyWords: FormControl<string>;
}>;

/** The whole product, as four steps' worth of controls. */
export type ProductForm = FormGroup<{
  sku: FormControl<string>;
  visible: FormControl<boolean>;
  canBePurchased: FormControl<boolean>;
  shipeable: FormControl<boolean>;
  virtual: FormControl<boolean>;
  dateAvailable: FormControl<string>;
  sortOrder: FormControl<number>;
  price: FormControl<number | null>;
  quantity: FormControl<number>;
  weight: FormControl<number | null>;
  height: FormControl<number | null>;
  width: FormControl<number | null>;
  length: FormControl<number | null>;
  weightUnit: FormControl<string>;
  dimensionUnit: FormControl<string>;
  typeCode: FormControl<string>;
  brandCode: FormControl<string>;
  copy: FormArray<ProductCopyForm>;
}>;

/**
 * What a SKU may be.
 *
 * Letters, digits, hyphen, underscore and dot. The pod accepts anything a `String` holds, but a SKU
 * is a URL segment on `…/product/unique?code=` and an identifier in every export, and one with a
 * space or a slash in it is a support ticket waiting to happen.
 */
export const SKU_PATTERN = /^[A-Za-z0-9._-]+$/;

/**
 * The product form.
 *
 * Follows `store-settings-form.service.ts`: the service builds and patches, the facade owns the
 * instance and the components only bind. Categories, images and related products are **not**
 * controls — each is its own set of endpoints and the first two are not even expressible as a form
 * value, so the facade holds them as signals and the steps bind to those.
 *
 * **What is not here is as deliberate as what is.** No barcode, no compare-at price, no unit cost or
 * derived margin, no bulk pricing tiers, no tax class, no per-product currency, no per-location
 * opening quantity or reorder point, no collections, no tags, no supplier, no backorder flag. The
 * design draws every one of them and the platform records none. Module 5's "keep them, marked"
 * approach does not scale to a form with this many dead controls — twelve disabled fields would
 * make the real ones hard to find — so they are removed, each with an entry in lessons.md.
 */
@Injectable({providedIn: 'root'})
export class ProductDraftFormService {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ProductFormApi);

  create(): ProductForm {
    return this.fb.nonNullable.group({
      /*
       * 255, matching `product.sku`. It was 100 — a limit this console invented, stricter than the
       * column and therefore refusing SKUs the platform would have accepted.
       *
       * The uniqueness check is an async validator rather than the hand-rolled `Subject` this used
       * to drive, so it reaches the operator through `app-field-error` like every other failure and
       * blocks submit through `form.pending` like every other async check.
       */
      sku: [
        '',
        [Validators.required, Validators.maxLength(SKU_MAX), Validators.pattern(SKU_PATTERN)],
        [this.uniqueSku()],
      ],
      visible: [false],
      canBePurchased: [true],
      shipeable: [true],
      virtual: [false],
      dateAvailable: [''],
      sortOrder: [0, [Validators.min(0)]],
      // Nullable on purpose: an empty price field is "not priced yet", which is a real state for a
      // draft, and is not the same as zero.
      price: this.fb.control<number | null>(null, [Validators.min(0)]),
      quantity: [0, [Validators.min(0)]],
      weight: this.fb.control<number | null>(null, [Validators.min(0)]),
      height: this.fb.control<number | null>(null, [Validators.min(0)]),
      width: this.fb.control<number | null>(null, [Validators.min(0)]),
      length: this.fb.control<number | null>(null, [Validators.min(0)]),
      weightUnit: ['KG'],
      dimensionUnit: ['CM'],
      /*
       * Empty string, not null, and both are bound with `formControlName`.
       *
       * A `<select>` bound with a plain `[value]` is set before its `@for` options exist, so the
       * browser discards the value and falls back to the first option — which is how a product with
       * a brand rendered as "No brand" until this was caught in QA. `SelectControlValueAccessor`
       * registers each option as it appears and re-applies the value, which is the only reliable
       * way to bind a select whose options arrive asynchronously.
       *
       * `''` is the "none" option's value, mapped back to `null` at the API boundary.
       */
      typeCode: [''],
      // Not required by the server, which accepts a product with no manufacturer. It is on the
      // readiness checklist as a recommendation, because a storefront that cannot name a product's
      // brand is a worse outcome than a form that asks for one.
      brandCode: [''],
      copy: this.fb.array<ProductCopyForm>([]),
    }) as ProductForm;
  }

  /**
   * One language's row in the copy array.
   *
   * Every length is its column's, from `schema.sql`. Nothing between here and the database checks
   * them, so an over-long value comes back as a driver error dressed up as a 500.
   */
  copyRow(copy: LocalisedCopy): ProductCopyForm {
    return this.fb.nonNullable.group({
      // Held as a control rather than as an array index, so a language added mid-edit cannot
      // silently shift every row's meaning by one.
      language: [copy.language],
      name: [copy.name, [Validators.maxLength(NAME_MAX)]],
      friendlyUrl: [copy.friendlyUrl, [Validators.maxLength(SLUG_MAX)]],
      highlights: [copy.highlights, [Validators.maxLength(HIGHLIGHTS_MAX)]],
      // No bound: `product_description.description` is `text`, and it is the one field that holds a
      // whole document.
      description: [copy.description],
      title: [copy.title, [Validators.maxLength(TITLE_MAX)]],
      metaDescription: [copy.metaDescription, [Validators.maxLength(META_DESCRIPTION_MAX)]],
      keyWords: [copy.keyWords, [Validators.maxLength(KEYWORDS_MAX)]],
    });
  }

  /**
   * Whether a SKU is already taken in this store.
   *
   * The pipeline is `@shared/forms/unique-async`, which the catalogue's code check and
   * create-store's name check also use — three copies of the same five operators before that, each
   * copied from the last.
   *
   * The `control.enabled` guard it carries is the one that matters here: the SKU is disabled once
   * the product exists, but the form is filled before that happens, so the check starts on the
   * product's own SKU and the server truthfully answers "yes, that exists". `disable()` nulls the
   * errors present at the time and cannot null one still in flight, so every saved product carried
   * a duplicate-SKU warning about itself.
   */
  private uniqueSku(): AsyncValidatorFn {
    return uniqueAsync((sku) => this.api.skuTaken(sku), 'skuTaken', {
      debounceMs: UNIQUENESS_DEBOUNCE_MS,
      when: (sku) => SKU_PATTERN.test(sku),
    });
  }

  /** Loads a draft into the form, replacing the copy rows outright. */
  fill(form: ProductForm, draft: ProductDraft): void {
    form.reset({
      sku: draft.sku,
      visible: draft.visible,
      canBePurchased: draft.canBePurchased,
      shipeable: draft.shipeable,
      virtual: draft.virtual,
      dateAvailable: draft.dateAvailable,
      sortOrder: draft.sortOrder,
      price: draft.price,
      quantity: draft.quantity,
      weight: draft.weight,
      height: draft.height,
      width: draft.width,
      length: draft.length,
      weightUnit: draft.weightUnit,
      dimensionUnit: draft.dimensionUnit,
      typeCode: draft.typeCode ?? '',
      brandCode: draft.brandCode ?? '',
    });

    const copy = form.controls.copy;
    copy.clear({emitEvent: false});
    for (const entry of draft.copy) {
      copy.push(this.copyRow(entry), {emitEvent: false});
    }
    copy.updateValueAndValidity();
  }

  /**
   * The form's value as a draft again.
   *
   * `getRawValue` rather than `value`: a disabled control is omitted from `value`, and the SKU is
   * disabled once the product exists — dropping it would send a product with no SKU.
   */
  toDraft(form: ProductForm, id: number | null, categoryIds: readonly number[], draft: ProductDraft): ProductDraft {
    const value = form.getRawValue();
    return {
      ...draft,
      id,
      sku: value.sku,
      visible: value.visible,
      canBePurchased: value.canBePurchased,
      shipeable: value.shipeable,
      virtual: value.virtual,
      dateAvailable: value.dateAvailable,
      sortOrder: value.sortOrder,
      price: value.price,
      quantity: value.quantity,
      weight: value.weight,
      height: value.height,
      width: value.width,
      length: value.length,
      weightUnit: value.weightUnit,
      dimensionUnit: value.dimensionUnit,
      // Back to `null`: the wire omits the field entirely rather than sending an empty code.
      typeCode: value.typeCode || null,
      brandCode: value.brandCode || null,
      categoryIds,
      copy: value.copy.map((entry) => ({
        language: entry.language,
        name: entry.name,
        friendlyUrl: entry.friendlyUrl,
        highlights: entry.highlights,
        description: entry.description,
        title: entry.title,
        metaDescription: entry.metaDescription,
        keyWords: entry.keyWords,
      })),
    };
  }
}
