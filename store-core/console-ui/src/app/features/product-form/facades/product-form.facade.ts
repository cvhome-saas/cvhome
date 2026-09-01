import {DestroyRef, Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {map, startWith} from 'rxjs';

import {ApiErrorService, clearServerErrorsOnChange} from '@cvhome-saas/ui-kit';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  COPY_FIELD_COUNT,
  MAX_VARIANTS,
  MAX_VARIANT_OPTIONS,
  PRODUCT_STEPS,
  VARIANT_SKU_PATTERN,
  combinationSignature,
  emptyDraft,
  type ProductDraft,
  type ProductImageItem,
  type ProductStep,
  type ReadinessItem,
  type RelatedProduct,
  type StoreOption,
  type StoreOptionValue,
  type TranslationRow,
  type VariantMatrixRow,
} from '@models/products';
import type {PersistableProductImage, PersistableVariantSet} from '@models/catalog';
import type {AutocompleteOption, StepItem} from '@cvhome-saas/ui-kit/ui';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {ProductsCache} from '@api/catalog/products-cache';
import {ProductSearch} from '@api/catalog/product-search.service';
import {
  ProductFormApi,
  type CategoryOption,
  type PendingVariantInventory,
  type ProductFormSnapshot,
  type ProductTypeOption,
} from '../services/product-form.api.service';
import {ProductDraftFormService} from '../services/product-draft-form.service';

/**
 * The product wizard's data, its form and its writes.
 *
 * **Not `providedIn: 'root'`.** Provided by the page component — the console's convention — and it
 * matters more here than anywhere: leaving `/products/new` and coming back gives a genuinely new
 * form rather than the last product's half-typed one. A root singleton here would carry an
 * abandoned draft across products, which is the kind of bug that only shows up as a support ticket.
 */
@Injectable()
export class ProductFormFacade {
  private readonly api = inject(ProductFormApi);
  private readonly formService = inject(ProductDraftFormService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly reference = inject(ReferenceDataService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly list = inject(ProductsCache);
  private readonly search = inject(ProductSearch);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly form = this.formService.create();

  /**
   * What the form currently holds.
   *
   * A `FormControl`'s value is not a signal, so the readiness checklist, the step ticks and the
   * translations panel — all of which have to move as the operator types — cannot read it through a
   * plain `computed`. This is the bridge. `getRawValue` rather than the emitted value because the
   * SKU control is disabled once the product exists, and `value` omits disabled controls.
   */
  private readonly formValue = toSignal(
    this.form.valueChanges.pipe(
      map(() => this.form.getRawValue()),
      startWith(this.form.getRawValue()),
    ),
    {initialValue: this.form.getRawValue()},
  );

  /** The product being edited, or `null` on `/products/new`. */
  readonly productId = signal<number | null>(null);
  /**
   * Whether the route has been read.
   *
   * `productId` cannot express this on its own: `null` is a legitimate value — it is `/products/new`
   * — so "no id yet" and "no id at all" look identical from here. The page sets this once it has
   * decided which of the two it is looking at.
   */
  readonly routeSettled = signal(false);
  readonly activeStep = signal<ProductStep>('essentials');

  constructor() {
    clearServerErrorsOnChange(this.form, this.destroyRef);
  }

  /* -------------------------------------------------------------------------- load ---- */

  /**
   * The product, its reference data and the store's languages.
   *
   * `params` returns `undefined` — which `rxResource` treats as "not ready", skipping the run —
   * until both the route and the store are known. Both arrive asynchronously and independently, and
   * keying the resource on them directly meant every one of them re-ran the whole `forkJoin` and
   * cancelled the previous attempt. The page issued eighteen requests to answer six questions.
   */
  private readonly snapshot = rxResource({
    params: () => {
      const store = this.shell.currentStoreId();
      if (!this.routeSettled() || !store) {
        return undefined;
      }
      return {id: this.productId(), store};
    },
    stream: ({params}) => this.api.load(params.id),
  });

  private readonly loaded = linkedSignal<ProductFormSnapshot | undefined, ProductFormSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);
  readonly saving = signal(false);

  readonly categories = computed<readonly CategoryOption[]>(() => this.loaded()?.categories ?? []);
  readonly brands = computed(() => this.loaded()?.brands ?? []);
  readonly types = computed<readonly ProductTypeOption[]>(() => this.loaded()?.types ?? []);
  readonly currency = computed(() => this.loaded()?.currency ?? null);

  readonly languages = computed<readonly ReferenceOption[]>(() =>
    this.formValue().copy.map((copy) => ({
      code: copy.language,
      label: this.reference.languageName(copy.language),
    })),
  );

  /**
   * The product as the last response described it.
   *
   * The form is the truth for everything it holds; this is the truth for the three things it does
   * not — the category set, the gallery and the related list — and the baseline a save is built on.
   */
  private readonly draftState = linkedSignal<ProductFormSnapshot | undefined, ProductDraft>({
    source: this.loaded,
    computation: (snapshot) => snapshot?.draft ?? emptyDraft(['en']),
  });

  readonly draft = this.draftState.asReadonly();

  /** Which categories are selected. Applied by diffing on save — see `ProductFormApi`. */
  readonly selectedCategories = linkedSignal<ProductDraft, readonly number[]>({
    source: this.draft,
    computation: (draft) => draft.categoryIds,
  });

  readonly images = linkedSignal<ProductDraft, readonly ProductImageItem[]>({
    source: this.draft,
    computation: (draft) => draft.images,
  });

  readonly related = linkedSignal<ProductDraft, readonly RelatedProduct[]>({
    source: this.draft,
    computation: (draft) => draft.related,
  });

  /* -------------------------------------------------------------------- variants ---- */

  /** The store's option vocabulary, for the axis picker. */
  readonly vocabulary = computed<readonly StoreOption[]>(() => this.loaded()?.vocabulary ?? []);

  /**
   * The options this product varies by, as full vocabulary entries in display order.
   *
   * Seeded from the snapshot once per product by {@link syncForm}, and re-seeded only when a
   * variants save reloads the truth. It used to be a `linkedSignal` off `loaded`, which meant
   * *any* assignment to `loaded` reset it — so pressing the header's Save draft with an unsaved
   * matrix on screen silently discarded the whole thing and reported success.
   */
  readonly variantAxes = signal<readonly StoreOption[]>([]);

  /** The matrix — one row per combination the product sells, price/stock merged in. */
  readonly variantRows = signal<readonly VariantMatrixRow[]>([]);

  /** The product the matrix was last seeded for; `undefined` means never. */
  private matrixSeededFor: number | null | undefined = undefined;

  /**
   * Whether this product varies by options — the switch the pricing step, the readiness list and
   * the save path all key on. Derived from the operator's current picks, never stored: the server
   * has no such flag either.
   */
  readonly hasOptions = computed(() => this.variantAxes().length > 0);

  /**
   * A variant save whose catalog half landed and whose inventory half did not.
   *
   * The step renders this as a named, retryable error — the explicit orchestration the module plan
   * demands in place of the old silent best-effort legs.
   */
  readonly variantInventoryPending = signal(false);

  /** What that failed half was writing, so the retry replays the intent and not the screen. */
  private readonly pendingInventory = signal<PendingVariantInventory | null>(null);

  /** Whether the server currently holds combination variants — what "Remove all options" undoes. */
  readonly hasSavedVariants = computed(() => (this.loaded()?.variants.length ?? 0) > 0);

  /**
   * The variant read failed, so what this product sells is unknown.
   *
   * Everything the step offers writes the *whole* set, so an empty matrix drawn over an unread one
   * would replace real combinations with whatever the operator generated. Saving is refused while
   * this holds, and the step says why.
   */
  readonly variantsUnavailable = computed(() => this.loaded()?.variantsUnavailable ?? false);

  /** The axis picker could not be filled — not the same as the store having no options. */
  readonly vocabularyUnavailable = computed(() => this.loaded()?.vocabularyUnavailable ?? false);

  /** Whether the step may write at all. */
  readonly canSaveVariants = computed(() => !this.variantsUnavailable() && !this.vocabularyUnavailable());

  /** Whether the form has been loaded from a draft yet — guards against binding an empty copy array. */
  private filledFor: number | null | undefined = undefined;

  /**
   * Load the form from the snapshot, once per product.
   *
   * Called from the page's `afterNextRender`-free effect rather than done inside a `computed`,
   * because filling a form is a write and a `computed` must not have one.
   */
  syncForm(): void {
    const snapshot = this.loaded();
    if (!snapshot) {
      return;
    }
    // Guarded separately: the matrix re-seeds after a variants save, when the form must not.
    this.seedMatrix(snapshot);
    if (this.filledFor === this.productId()) {
      return;
    }
    this.filledFor = this.productId();
    this.formService.fill(this.form, snapshot.draft);
    if (this.productId() !== null) {
      // A SKU identifies the product in every export and on the uniqueness endpoint. Changing it
      // after the fact is not something this form offers.
      this.form.controls.sku.disable({emitEvent: false});
    }
  }

  /**
   * Reload the snapshot and re-seed the matrix from it.
   *
   * Used only after a variants save that fully landed: that is the one moment the server's matrix
   * is better than the operator's, because it carries the ids the new rows were given.
   */
  private reseedFromServer(): void {
    this.matrixSeededFor = undefined;
    this.snapshot.reload();
  }

  /**
   * Seed the matrix from the server's truth — once per product, and again only when a variants
   * save has reloaded it. `force` is that second case.
   *
   * Anything else (a draft save, a categories save) must leave the operator's unsaved matrix
   * alone: the two are edited on the same screen and saved by different buttons.
   */
  private seedMatrix(snapshot: ProductFormSnapshot, force = false): void {
    if (!force && this.matrixSeededFor === this.productId()) {
      return;
    }
    this.matrixSeededFor = this.productId();
    const byId = new Map(snapshot.vocabulary.map((option) => [option.id, option]));
    this.variantAxes.set(
      snapshot.assignedOptionIds
        .map((id) => byId.get(id))
        .filter((option): option is StoreOption => option !== undefined),
    );
    this.variantRows.set(snapshot.variants);
  }

  /* ---------------------------------------------------------------------- the rail ---- */

  /**
   * The four steps, with what is done and what cannot be entered yet.
   *
   * Media and the related-products block need a product id, because images post to
   * `…/product/{id}/image` and relationships to `…/products/{id}/relationship/{id}`. Rather than
   * let an operator reach a dead control, the rail says so and Save draft is the way through.
   */
  readonly steps = computed<readonly StepItem[]>(() => {
    this.transloco.activeLang();
    const saved = this.productId() !== null;
    return PRODUCT_STEPS.map((step) => ({
      key: step,
      label: this.transloco.translate(`productForm.step.${step}`),
      meta:
        (step === 'media' || step === 'variants') && !saved
          ? this.transloco.translate(`productForm.step.${step}Locked`)
          : this.transloco.translate(`productForm.step.${step}Meta`),
      // Both attach to `…/product/{id}/…` endpoints, so there is nothing to edit until it exists.
      disabled: (step === 'media' || step === 'variants') && !saved,
      complete: this.stepComplete(step, saved),
    }));
  });

  private stepComplete(step: ProductStep, saved: boolean): boolean {
    const value = this.formValue();
    switch (step) {
      case 'essentials':
        return value.sku.trim() !== '' && value.copy.some((copy) => copy.name.trim() !== '');
      case 'media':
        return saved && this.images().length > 0;
      case 'pricing':
        // Superseded for a product with options: its prices live one row per variant.
        return this.hasOptions() || (value.price !== null && value.price > 0);
      case 'variants':
        return this.hasOptions() && this.variantRows().length > 0 && this.variantsPriced();
      case 'organize':
        return this.selectedCategories().length > 0 && value.brandCode !== '';
    }
  }

  /** Whether every matrix row has a price — the multi-variant reading of "priced". */
  private variantsPriced(): boolean {
    return this.variantRows().every((row) => row.price !== null && row.price > 0);
  }

  /* ------------------------------------------------------------------- readiness ---- */

  /**
   * The publish checklist.
   *
   * Computed from the form's own fields, so it costs no endpoint and cannot disagree with what is
   * on screen. `required` marks a blocker for publishing; the rest are recommendations.
   */
  readonly readiness = computed<readonly ReadinessItem[]>(() => {
    const saved = this.productId() !== null;
    const value = this.formValue();
    return [
      {key: 'sku', done: value.sku.trim() !== '', required: true},
      /*
       * Every language, not one of them. The populator replaces a product's description list
       * wholesale, so a language saved blank is a language cleared — the storefront then has nothing
       * to render for those shoppers. This is also what `missingLanguages` blocks the save on, and
       * the two must agree or the panel would show a tick beside a refused Save.
       */
      {key: 'name', done: value.copy.every((copy) => copy.name.trim() !== ''), required: true},
      /*
       * The multi-variant generalisation of "it has a price": every combination sku must have its
       * inventory row priced, or the storefront will render sellable chips with no figure behind
       * them. For a simple product it stays the one price field.
       */
      this.hasOptions()
        ? {key: 'variantPricing', done: this.variantRows().length > 0 && this.variantsPriced(), required: true}
        : {key: 'price', done: value.price !== null && value.price > 0, required: true},
      {key: 'category', done: this.selectedCategories().length > 0, required: true},
      {key: 'brand', done: value.brandCode !== '', required: false},
      {key: 'image', done: saved && this.images().length > 0, required: false},
      {key: 'description', done: value.copy.some((copy) => copy.description.trim() !== ''), required: false},
    ];
  });

  readonly readyPercent = computed(() => {
    const items = this.readiness();
    return items.length ? Math.round((items.filter((item) => item.done).length / items.length) * 100) : 0;
  });

  /** Whether every required item is done. Publishing is refused until it is. */
  readonly canPublish = computed(() => this.readiness().every((item) => !item.required || item.done));

  /**
   * One row per language, with how much of its copy is written.
   *
   * The console can see exactly which locales a product is missing, which the feature inventory
   * flags as invisible in seller-ui. Client-side, off `descriptions[]`, so it costs nothing.
   */
  readonly translations = computed<readonly TranslationRow[]>(() =>
    this.formValue().copy.map((copy) => ({
      language: copy.language,
      name: this.reference.languageName(copy.language),
      filled: [copy.name, copy.description, copy.friendlyUrl, copy.title, copy.metaDescription].filter(
        (field) => field.trim() !== '',
      ).length,
      total: COPY_FIELD_COUNT,
    })),
  );

  /**
   * The languages this product has no name in.
   *
   * Named, so the refusal can say "Arabic has no name" rather than sending the operator through
   * four steps looking for an empty field that is not on the step they are standing on.
   */
  readonly missingLanguages = computed<readonly string[]>(() =>
    this.formValue()
      .copy.filter((copy) => copy.name.trim() === '')
      .map((copy) => this.reference.languageName(copy.language)),
  );

  readonly canSave = computed(() => this.missingLanguages().length === 0);

  /* --------------------------------------------------------------------- writing ---- */

  /**
   * Save without publishing.
   *
   * On a new product this is what creates it: a `POST` with `visible: false`, followed by a
   * navigation to `/products/:id` where Media and Related come alive. On an existing one it is an
   * ordinary save that leaves `visible` alone.
   */
  saveDraft(): void {
    this.persist(false);
  }

  /** Save and put it on the storefront. Refused until every required item on the checklist is done. */
  publish(): void {
    if (!this.canPublish()) {
      this.form.markAllAsTouched();
      this.toast.warning(this.transloco.translate('productForm.cannotPublish'));
      return;
    }
    this.persist(true);
  }

  private persist(visible: boolean): void {
    /*
     * Checked before `form.invalid`, and separately from it: the missing name is in a language the
     * operator may not have open, so marking the form touched would highlight nothing. The toast
     * names the languages instead.
     */
    if (!this.canSave()) {
      this.toast.danger(
        this.transloco.translate('productForm.missingLanguages', {
          languages: this.missingLanguages().join(', '),
        }),
      );
      this.activeStep.set('essentials');
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.activeStep.set('essentials');
      return;
    }

    const id = this.productId();
    const draft = {
      ...this.formService.toDraft(this.form, id, this.selectedCategories(), this.draft()),
      // Publishing sets it; Save draft on an existing product leaves whatever the product had.
      visible: visible || (id !== null && this.form.controls.visible.value),
    };

    this.saving.set(true);
    if (id === null) {
      this.api.create(draft).subscribe({
        next: ({id: createdId, categoriesApplied}) => {
          this.saving.set(false);
          this.list.invalidate();
          /*
           * The product exists either way. If the category diff failed after it was created, saying
           * so is honest and routing anyway is what stops the operator retrying into a duplicate
           * SKU — the Organize step shows the truth as soon as it reloads.
           */
          if (categoriesApplied) {
            this.toast.success(this.transloco.translate('productForm.saved.created'));
          } else {
            this.toast.warning(this.transloco.translate('productForm.saved.createdWithoutCategories'));
          }
          // Where Media and Related become reachable. The rail said this would happen.
          this.router.navigate(['/products', createdId]);
        },
        error: (failure: unknown) => this.fail(failure),
      });
      return;
    }

    this.api.update(id, draft, !this.hasOptions()).subscribe({
      next: ({snapshot, categoriesApplied}) => {
        this.saving.set(false);
        this.loaded.set(snapshot);
        this.draftState.set(snapshot.draft);
        this.list.invalidate();
        if (!categoriesApplied) {
          // The definition landed; the categories did not. The form now shows what the server has.
          this.toast.warning(this.transloco.translate('productForm.saved.categoriesFailed'));
          return;
        }
        this.toast.success(
          this.transloco.translate(visible ? 'productForm.saved.published' : 'productForm.saved.updated'),
        );
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  private fail(failure: unknown): void {
    this.saving.set(false);
    this.apiErrors.applyToForm(failure, this.form);
  }

  /* -------------------------------------------------------- variants: editing ---- */

  /**
   * Add an axis: this product now varies by `optionId` too.
   *
   * The matrix regenerates immediately — the full cartesian product, existing combinations kept by
   * signature — because a picked axis with no rows behind it is a state the atomic PUT cannot even
   * express, and the operator's next question is always "what combinations is that?".
   */
  addVariantAxis(optionId: number): void {
    const option = this.vocabulary().find((candidate) => candidate.id === optionId);
    if (!option || this.variantAxes().some((axis) => axis.id === optionId)) {
      return;
    }
    if (this.variantAxes().length >= MAX_VARIANT_OPTIONS) {
      this.toast.warning(
        this.transloco.translate('productForm.variants.optionLimit', {max: MAX_VARIANT_OPTIONS}),
      );
      return;
    }
    if (option.values.length === 0) {
      // Defined in the catalogue with no values yet — there is nothing to combine.
      this.toast.warning(this.transloco.translate('productForm.variants.optionHasNoValues'));
      return;
    }
    this.variantAxes.set([...this.variantAxes(), option]);
    this.regenerateMatrix();
  }

  /** Drop an axis. With no axes left the matrix empties — saving that restores the default variant. */
  removeVariantAxis(optionId: number): void {
    this.variantAxes.set(this.variantAxes().filter((axis) => axis.id !== optionId));
    this.regenerateMatrix();
  }

  /** One field of one row — the matrix cells write through here, immutably. */
  updateVariantRow(index: number, patch: Partial<VariantMatrixRow>): void {
    this.variantRows.set(
      this.variantRows().map((row, at) => (at === index ? {...row, ...patch} : row)),
    );
  }

  removeVariantRow(index: number): void {
    const rows = this.variantRows().filter((_, at) => at !== index);
    this.variantRows.set(ensureOneDefault(rows));
  }

  /** The default radio: the card/list price and the PDP preselection. Exactly one, always. */
  setDefaultVariant(index: number): void {
    this.variantRows.set(
      this.variantRows().map((row, at) => ({...row, isDefault: at === index})),
    );
  }

  /**
   * The escape hatch beside the generator: re-add one combination (for a row removed earlier, or
   * after a generation was clipped by the cap). `valueIds` carries one value per axis, in order.
   */
  addVariantCombination(valueIds: readonly number[]): void {
    const axes = this.variantAxes();
    if (valueIds.length !== axes.length || valueIds.some((id) => id === 0)) {
      return;
    }
    const signature = combinationSignature(valueIds);
    if (this.variantRows().some((row) => combinationSignature(row.optionValueIds) === signature)) {
      this.toast.warning(this.transloco.translate('productForm.variants.duplicateCombination'));
      return;
    }
    if (this.variantRows().length >= MAX_VARIANTS) {
      this.toast.warning(
        this.transloco.translate('productForm.variants.variantLimit', {max: MAX_VARIANTS}),
      );
      return;
    }
    const values = axes.map(
      (axis, at) => axis.values.find((value) => value.id === valueIds[at]) as StoreOptionValue,
    );
    const row: VariantMatrixRow = {
      id: null,
      sku: this.suggestVariantSku(values),
      optionValueIds: values.map((value) => value.id),
      labels: values.map((value) => value.name),
      isDefault: false,
      price: null,
      quantity: 0,
      available: true,
    };
    this.variantRows.set(ensureOneDefault([...this.variantRows(), row]));
  }

  /**
   * Rebuild the matrix as the cartesian product of the chosen axes' values.
   *
   * Existing rows are matched by combination signature and kept whole — their ids (the catalog
   * rows), skus, prices and stock survive an axis reorder. The first generation of a fresh matrix
   * seeds its first row from the product's own sku, price and quantity, so a simple product's
   * stock carries over into its first combination instead of silently starting from zero.
   */
  private regenerateMatrix(): void {
    const axes = this.variantAxes();
    if (axes.length === 0) {
      this.variantRows.set([]);
      return;
    }
    const existing = new Map(
      this.variantRows().map((row) => [combinationSignature(row.optionValueIds), row]),
    );
    const previous = this.variantRows();
    const fresh = existing.size === 0;
    const value = this.formValue();
    const rows: VariantMatrixRow[] = [];
    let clipped = false;
    for (const combination of cartesian(axes.map((axis) => axis.values))) {
      if (rows.length >= MAX_VARIANTS) {
        clipped = true;
        break;
      }
      const ids = combination.map((entry) => entry.id);
      const labels = combination.map((entry) => entry.name);
      const kept = existing.get(combinationSignature(ids)) ?? overlapping(previous, ids);
      if (kept) {
        /*
         * Re-projected onto the new combination; everything the operator set survives. An exact
         * signature match covers a reorder; `overlapping` covers the axis set *changing*, which
         * is the common edit — adding Size to a colour-only product used to null every price, id
         * and quantity, and the save then deleted the old inventory rows and wrote nothing back
         * because `applyVariantInventory` skips unpriced rows.
         */
        rows.push({
          ...kept,
          // Only the row that still *is* this combination keeps its catalog row and its sku.
          id: kept.optionValueIds.length === ids.length ? kept.id : null,
          sku: kept.optionValueIds.length === ids.length
            ? kept.sku
            : this.suggestVariantSku(combination, rows),
          optionValueIds: ids,
          labels,
        });
        continue;
      }
      const seed = fresh && rows.length === 0;
      rows.push({
        id: null,
        sku: seed ? this.baseVariantSku() : this.suggestVariantSku(combination, rows),
        optionValueIds: ids,
        labels,
        isDefault: false,
        price: seed ? value.price : null,
        quantity: seed ? value.quantity : 0,
        available: true,
      });
    }
    this.variantRows.set(ensureOneDefault(rows));
    if (clipped) {
      this.toast.warning(
        this.transloco.translate('productForm.variants.variantLimit', {max: MAX_VARIANTS}),
      );
    }
  }

  /** The product's own sku, made legal for a variant (the variant pattern allows no dot). */
  private baseVariantSku(): string {
    return (this.formValue().sku || 'SKU').replace(/[^A-Za-z0-9_-]+/g, '-');
  }

  /** `<productSku>-<VALUECODES>`, uniquified against the rows already generated. Editable after. */
  private suggestVariantSku(
    combination: readonly StoreOptionValue[],
    rows: readonly VariantMatrixRow[] = this.variantRows(),
  ): string {
    const suffix = combination
      .map((value) => value.code.toUpperCase().replace(/[^A-Z0-9_-]+/g, ''))
      .filter(Boolean)
      .join('-');
    const candidate = `${this.baseVariantSku()}-${suffix}`;
    const taken = new Set(rows.map((row) => row.sku));
    if (!taken.has(candidate)) {
      return candidate;
    }
    let attempt = 2;
    while (taken.has(`${candidate}-${attempt}`)) {
      attempt += 1;
    }
    return `${candidate}-${attempt}`;
  }

  /* --------------------------------------------------------- variants: saving ---- */

  /**
   * Save the axes and the matrix — the step's own save, separate from Save draft because it is a
   * different transaction against a different pair of services.
   *
   * Client-side checks mirror the pod's named refusals so the operator gets the row, not a 409:
   * a blank or illegal sku, a duplicate sku, an empty matrix under declared axes. The write itself
   * is `ProductFormApi.saveVariants` — catalog PUT first (atomic), inventory second (reported
   * honestly, retryable via `retryVariantInventory`).
   */
  saveVariants(): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    if (!this.canSaveVariants()) {
      // The write replaces the whole set, and we do not know what the set currently is.
      this.toast.danger(this.transloco.translate('productForm.variants.unreadable'));
      return;
    }
    const axes = this.variantAxes();
    const rows = this.variantRows();
    if (axes.length > 0 && rows.length === 0) {
      this.toast.danger(this.transloco.translate('productForm.variants.needsRow'));
      return;
    }
    const illegal = rows.find((row) => !VARIANT_SKU_PATTERN.test(row.sku));
    if (illegal) {
      this.toast.danger(
        this.transloco.translate('productForm.variants.invalidSku', {sku: illegal.sku || '—'}),
      );
      return;
    }
    if (new Set(rows.map((row) => row.sku)).size !== rows.length) {
      this.toast.danger(this.transloco.translate('productForm.variants.duplicateSku'));
      return;
    }

    const set: PersistableVariantSet = {
      options: axes.map((axis) => axis.code),
      variants: rows.map((row, index) => ({
        ...(row.id !== null ? {id: row.id} : {}),
        sku: row.sku,
        sortOrder: index,
        defaultVariant: row.isDefault,
        optionValueIds: [...row.optionValueIds],
      })),
    };

    this.saving.set(true);
    this.api.saveVariants(id, set, rows).subscribe({
      next: ({inventoryApplied, pendingInventory}) => {
        this.saving.set(false);
        this.list.invalidate();
        this.variantInventoryPending.set(!inventoryApplied);
        this.pendingInventory.set(pendingInventory);
        if (inventoryApplied) {
          this.toast.success(this.transloco.translate('productForm.variants.saved'));
          // Only now is the server's matrix the better truth — it carries the new rows' ids.
          this.reseedFromServer();
          return;
        }
        /*
         * The catalog now says one thing and inventory another. Named, and retryable in place —
         * and deliberately NOT reloaded: the reload would replace the prices the operator just
         * typed with the ones the failed write never changed, and the banner would then be
         * offering to "retry" writing the old numbers back.
         */
        this.toast.warning(this.transloco.translate('productForm.variants.inventoryFailed'));
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** Re-run only the inventory half of a variant save that reported `inventoryApplied: false`. */
  retryVariantInventory(): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    const pending = this.pendingInventory();
    if (!pending) {
      return;
    }
    this.saving.set(true);
    this.api.applyVariantInventory(id, pending.rows, pending.removedSkus).subscribe({
      next: (applied) => {
        this.saving.set(false);
        this.variantInventoryPending.set(!applied);
        if (applied) {
          this.pendingInventory.set(null);
          this.toast.success(this.transloco.translate('productForm.variants.saved'));
          this.reseedFromServer();
        } else {
          this.toast.warning(this.transloco.translate('productForm.variants.inventoryFailed'));
        }
      },
      error: () => {
        this.saving.set(false);
        this.variantInventoryPending.set(true);
      },
    });
  }

  /* ----------------------------------------------------------------------- media ---- */

  readonly uploading = signal(false);

  /** Attaches assets the operator picked in the media library. */
  attachImages(assets: readonly PersistableProductImage[]): void {
    const id = this.productId();
    if (id === null || !assets.length) {
      return;
    }
    this.uploading.set(true);
    this.api.attachImages(id, assets).subscribe({
      next: (gallery) => {
        this.uploading.set(false);
        this.images.set(gallery);
      },
      error: (failure: unknown) => {
        this.uploading.set(false);
        this.apiErrors.notify(failure);
        // A batch that failed partway may still have landed some of its writes. `ProductFormApi`
        // re-reads before re-throwing, so asking again is what puts the truth on screen.
        this.refreshGallery();
      },
    });
  }

  removeImage(imageId: number): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    this.uploading.set(true);
    this.api.removeImage(id, imageId).subscribe({
      next: (gallery) => {
        this.uploading.set(false);
        this.images.set(gallery);
      },
      error: (failure: unknown) => {
        this.uploading.set(false);
        this.apiErrors.notify(failure);
        // A batch that failed partway may still have landed some of its writes. `ProductFormApi`
        // re-reads before re-throwing, so asking again is what puts the truth on screen.
        this.refreshGallery();
      },
    });
  }

  /**
   * Move an image one place earlier or later.
   *
   * The whole list is renumbered server-side afterwards — `PATCH …?order=` does not renumber the
   * images it displaces, so writing only the moved one leaves two sharing a position.
   */
  moveImage(index: number, offset: number): void {
    const id = this.productId();
    const gallery = [...this.images()];
    const target = index + offset;
    if (id === null || target < 0 || target >= gallery.length) {
      return;
    }
    const [moved] = gallery.splice(index, 1);
    gallery.splice(target, 0, moved);

    /*
     * The first image is the thumbnail, so the move re-designates it.
     *
     * The old upload endpoint fixed the default at the first upload and had no way to move it, which
     * is why the step used to say so. `PUT …/product/{id}/images` takes the flag, so the constraint
     * is gone — and leaving it pinned to whichever image happened to be uploaded first was worse
     * than either rule: reordering carried the badge off position 1, so the panel said "the first
     * image is the storefront thumbnail" directly above a grid where it demonstrably was not.
     *
     * Reordering is therefore also the answer to "how do I change the thumbnail": move it to the
     * front. There is no separate control, because there is no separate concept.
     */
    const ordered = gallery.map((image, position) => ({...image, isDefault: position === 0}));

    this.uploading.set(true);
    this.api.replaceImages(id, ordered).subscribe({
      next: (reordered) => {
        this.uploading.set(false);
        this.images.set(reordered);
      },
      error: (failure: unknown) => {
        this.uploading.set(false);
        this.apiErrors.notify(failure);
        this.refreshGallery();
      },
    });
  }

  /**
   * Re-reads the product's gallery from the server.
   *
   * The recovery path for a failed image batch: the optimistic list is stale by definition once a
   * write has partly landed, and the only thing that knows what is actually there is the pod.
   */
  private refreshGallery(): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    this.api.load(id).subscribe({
      next: (snapshot) => this.images.set(snapshot.draft.images),
      // Nothing more to try. The toast for the original failure is already up.
      error: () => undefined,
    });
  }

  /* ---------------------------------------------------------------- organize ---- */

  toggleCategory(categoryId: number): void {
    const current = this.selectedCategories();
    this.selectedCategories.set(
      current.includes(categoryId)
        ? current.filter((id) => id !== categoryId)
        : [...current, categoryId],
    );
  }

  readonly relatedResults = signal<readonly AutocompleteOption[]>([]);
  readonly relatedSearching = signal(false);

  searchRelated(term: string): void {
    this.relatedSearching.set(true);
    this.search.find(term, this.productId()).subscribe({
      next: (products) => {
        const already = new Set(this.related().map((product) => product.id));
        this.relatedResults.set(
          products
            .filter((product) => !already.has(product.id))
            .map((product) => ({id: product.id, label: product.name, detail: product.sku})),
        );
        this.relatedSearching.set(false);
      },
      error: () => {
        this.relatedResults.set([]);
        this.relatedSearching.set(false);
      },
    });
  }

  addRelated(relatedId: number): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    this.relatedResults.set([]);
    this.saving.set(true);
    this.api.addRelated(id, relatedId).subscribe({
      next: (products) => {
        this.saving.set(false);
        this.related.set(products);
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  removeRelated(relatedId: number): void {
    const id = this.productId();
    if (id === null) {
      return;
    }
    this.saving.set(true);
    this.api.removeRelated(id, relatedId).subscribe({
      next: (products) => {
        this.saving.set(false);
        this.related.set(products);
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  retry(): void {
    this.snapshot.reload();
  }
}

/* ---------------------------------------------------------------------------- helpers ---- */

/** Every combination, in axis-major order — the order the matrix reads naturally. */
function cartesian(axes: readonly (readonly StoreOptionValue[])[]): readonly (readonly StoreOptionValue[])[] {
  return axes.reduce<readonly (readonly StoreOptionValue[])[]>(
    (built, values) => built.flatMap((row) => values.map((value) => [...row, value])),
    [[]],
  );
}

/** Exactly one default per set — the DB enforces it, this keeps the radio honest before the save. */
/**
 * The previous row that best describes this combination, when the axis set has changed.
 *
 * Adding an axis widens every combination (Red -> Red/M, Red/L): the old Red row describes both,
 * so its price and stock carry to both. Removing one narrows them (Red/M, Red/L -> Red): the first
 * old row that still overlaps wins, which is the one the operator was last looking at. Matching is
 * by value ids, so it survives a reorder of the axes as well.
 */
function overlapping(
  previous: readonly VariantMatrixRow[],
  ids: readonly number[],
): VariantMatrixRow | undefined {
  const wanted = new Set(ids);
  return previous.find((row) => {
    const held = row.optionValueIds;
    return held.length > 0
      && (held.every((id) => wanted.has(id)) || held.some((id) => wanted.has(id)));
  });
}

function ensureOneDefault(rows: readonly VariantMatrixRow[]): readonly VariantMatrixRow[] {
  if (rows.length === 0 || rows.filter((row) => row.isDefault).length === 1) {
    return rows;
  }
  // None flagged falls to the first row — the same lowest-sort-order fallback the service applies.
  const flagged = rows.findIndex((row) => row.isDefault);
  const target = flagged === -1 ? 0 : flagged;
  return rows.map((row, index) => ({...row, isDefault: index === target}));
}
