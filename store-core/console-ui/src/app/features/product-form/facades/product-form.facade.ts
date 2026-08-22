import {DestroyRef, Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {map, startWith} from 'rxjs';

import {ApiErrorService} from '@core/errors/api-error.service';
import {clearServerErrorsOnChange} from '@core/errors/form-error.utils';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  COPY_FIELD_COUNT,
  PRODUCT_STEPS,
  emptyDraft,
  type ProductDraft,
  type ProductImageItem,
  type ProductStep,
  type ReadinessItem,
  type RelatedProduct,
  type TranslationRow,
} from '@models/products';
import type {AutocompleteOption} from '@shared/ui/autocomplete/autocomplete';
import type {StepItem} from '@shared/ui/stepper/stepper';
import {ToastService} from '@shared/ui/toast/toast';
import {ProductsCache} from '@api/catalog/products-cache';
import {ProductSearch} from '@api/catalog/product-search.service';
import {ProductFormApi, type CategoryOption, type ProductFormSnapshot, type ProductTypeOption} from '../services/product-form.api.service';
import {ProductDraftFormService} from '../services/product-draft-form.service';

/**
 * The product wizard's data, its form and its writes.
 *
 * **Not `providedIn: 'root'`.** Unlike every other facade in this console, this one is provided by
 * the page component, so leaving `/products/new` and coming back gives a genuinely new form rather
 * than the last product's half-typed one. A root singleton here would carry an abandoned draft
 * across products, which is the kind of bug that only shows up as a support ticket.
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
    if (!snapshot || this.filledFor === this.productId()) {
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
        step === 'media' && !saved
          ? this.transloco.translate('productForm.step.mediaLocked')
          : this.transloco.translate(`productForm.step.${step}Meta`),
      disabled: step === 'media' && !saved,
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
        return value.price !== null && value.price > 0;
      case 'organize':
        return this.selectedCategories().length > 0 && value.brandCode !== '';
    }
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
      {key: 'price', done: value.price !== null && value.price > 0, required: true},
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

    this.api.update(id, draft).subscribe({
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

  /* ----------------------------------------------------------------------- media ---- */

  readonly uploading = signal(false);

  uploadImages(files: readonly File[]): void {
    const id = this.productId();
    if (id === null || !files.length) {
      return;
    }
    this.uploading.set(true);
    this.api.uploadImages(id, files, this.images()).subscribe({
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

    this.uploading.set(true);
    this.api.reorderImages(id, gallery).subscribe({
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
