import {DestroyRef, Injectable, computed, effect, inject, linkedSignal, signal} from '@angular/core';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {Observable} from 'rxjs';
import {AbstractControl} from '@angular/forms';

import {ApiErrorService} from '@core/errors/api-error.service';
import {clearServerErrorsOnChange} from '@core/errors/form-error.utils';
import {LocaleService} from '@core/i18n/locale.service';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  copyFor,
  emptyCopy,
  type BrandCard,
  type CatalogueSnapshot,
  type CatalogueTab,
  type CategoryNode,
  type GroupRow,
  type LocalisedCopy,
  type TypeCard,
} from '@models/taxonomy';
import type {AutocompleteOption} from '@shared/ui/autocomplete/autocomplete';
import type {TabItem} from '@shared/ui/tab-switcher/tab-switcher';
import type {TreeMove, TreeNode} from '@shared/ui/tree/tree';
import {ToastService} from '@shared/ui/toast/toast';
import {ProductSearch} from '@features/product-form/services/product-search';
import {CatalogueApi} from '../services/catalogue.api.service';
import {CatalogueFormService, slugify} from '../services/catalogue-form.service';

/** What the editor beside a list is doing: changing a record, or writing a new one. */
export type EditorMode = 'edit' | 'create';

/** A record queued for deletion, waiting on the confirm dialog. */
interface PendingDelete {
  readonly tab: CatalogueTab;
  /** A category, brand or type id; a group's code. */
  readonly key: number | string;
  readonly name: string;
}

/**
 * The catalogue's data and its four editors.
 *
 * Follows `StoreSettingsFacade`: an `rxResource` keyed on the current store, a `linkedSignal`
 * holding the last good snapshot so the page does not blank between requests, and forms owned here
 * rather than in a component because a selection change has to rewrite them.
 *
 * **Unsaved copy survives a language switch.** Each editor's form holds one language at a time, and
 * `draft` holds the rest. Without it, typing an English name, switching to Arabic and switching
 * back would lose the English — which is exactly what an operator translating a category does all
 * day. The draft is cleared on save and on selecting a different record, and never on a language
 * change.
 */
@Injectable({providedIn: 'root'})
export class CatalogueFacade {
  private readonly api = inject(CatalogueApi);
  private readonly search = inject(ProductSearch);
  private readonly forms = inject(CatalogueFormService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly locale = inject(LocaleService);
  private readonly reference = inject(ReferenceDataService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly destroyRef = inject(DestroyRef);

  /** Which tab is open. Set from the route, so a tab is linkable and survives a reload. */
  readonly activeTab = signal<CatalogueTab>('categories');

  readonly categoryForm = this.forms.category();
  readonly brandForm = this.forms.brand();
  readonly typeForm = this.forms.type();
  readonly groupForm = this.forms.group();

  constructor() {
    /*
     * A server-side field error is not a validator, so nothing else would ever clear it — the
     * operator would fix the field and find Save still refusing. Four forms, four subscriptions.
     */
    for (const form of [this.categoryForm, this.brandForm, this.typeForm, this.groupForm]) {
      clearServerErrorsOnChange(form, this.destroyRef);
      /*
       * The language chips and the save guard both have to react as the operator types, and the
       * copy for the language on screen lives in a form rather than in a signal — it only reaches
       * `draft` when the language is switched. This is what makes the live one visible to them.
       */
      form.controls.copy.valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.copyStamp.update((stamp) => stamp + 1));
    }

    /*
     * Load the active tab's form from whatever record is selected.
     *
     * An effect rather than a call at each of the six places selection can change, because one of
     * those places is **the first response arriving** — there is no user action there to hang a
     * call on, and without this the editor rendered empty beside a fully populated list until the
     * operator touched something. Caught in QA against the live stack.
     *
     * Filling a form is not a signal write, so this is an ordinary effect. `filledFor` makes it
     * idempotent: the effect re-runs whenever any of its dependencies change, and refilling on
     * every run would discard whatever the operator had typed.
     */
    effect(() => {
      const stamp = this.selectionStamp();
      if (stamp === null || this.filledFor === stamp) {
        return;
      }
      this.filledFor = stamp;
      this.refill();
    });
  }

  /**
   * Which record the editor should be showing, as one comparable value.
   *
   * `null` while creating — the form there is the operator's blank, and refilling it from the
   * previously selected record would wipe what they had started typing.
   */
  private readonly selectionStamp = computed<string | null>(() => {
    if (this.mode() === 'create') {
      return null;
    }
    const tab = this.activeTab();
    const language = this.activeLanguage();
    const key =
      tab === 'categories'
        ? this.selectedCategoryId()
        : tab === 'brands'
          ? this.selectedBrandId()
          : tab === 'types'
            ? this.selectedTypeId()
            : this.selectedGroupCode();
    return key === null ? null : `${tab}:${key}:${language}`;
  });

  /** The stamp the forms currently hold. See the effect above. */
  private filledFor: string | null = null;

  /* ------------------------------------------------------------------------- load ---- */

  /**
   * The catalogue, keyed on the store it belongs to.
   *
   * `params` returning `undefined` until the store directory resolves leaves the resource idle,
   * which is what stops the page firing one unscoped request and then the correct one — the same
   * shape `OrdersFacade` uses and for the same reason.
   */
  private readonly snapshot = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.api.load(),
  });

  private readonly loaded = linkedSignal<CatalogueSnapshot | undefined, CatalogueSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);

  /** True while a write is in flight. Locks the tree and every Save on the page. */
  readonly saving = signal(false);

  readonly categories = computed<readonly CategoryNode[]>(() => this.loaded()?.categories ?? []);
  readonly brands = computed<readonly BrandCard[]>(() => this.loaded()?.brands ?? []);
  readonly types = computed<readonly TypeCard[]>(() => this.loaded()?.types ?? []);
  readonly groups = computed<readonly GroupRow[]>(() => this.loaded()?.groups ?? []);

  /**
   * How many categories there are, counting the whole tree rather than its roots.
   *
   * One number, read by both the tab badge and the page header. They used to compute it separately
   * and disagree — the header said 4 (the roots) beside a tab that said 12.
   */
  readonly categoryCount = computed(() => flatten(this.categories()).length);

  /** Which tabs could not load. The tab says so rather than showing an empty list it did not read. */
  readonly unavailable = computed<readonly CatalogueTab[]>(() => this.loaded()?.unavailable ?? []);

  /* -------------------------------------------------------------------- languages ---- */

  /**
   * The languages this store's copy can be written in.
   *
   * The **store's** supported set, not the console's en/ar: these chips are about the shopper's
   * language. Falls back to the console's own locales only when the merchant pod could not be
   * reached, so the chips are never empty and the editor is never unusable.
   */
  readonly languages = computed<readonly ReferenceOption[]>(() => {
    const supported = this.loaded()?.languages ?? [];
    const codes = supported.length ? supported : this.locale.locales.map((entry) => entry.code);
    return codes.map((code) => ({code, label: this.reference.languageName(code)}));
  });

  /**
   * Which language is being written.
   *
   * `linkedSignal` so an explicit choice survives a reload but a store swap starts from that
   * store's first supported language rather than stranding the operator on a language the new
   * store does not trade in.
   */
  readonly activeLanguage = linkedSignal<readonly ReferenceOption[], string>({
    source: this.languages,
    computation: (languages, previous) => {
      const chosen = previous?.value;
      if (chosen && languages.some((language) => language.code === chosen)) {
        return chosen;
      }
      const preferred = this.locale.currentLocale().code;
      return languages.some((language) => language.code === preferred)
        ? preferred
        : (languages[0]?.code ?? preferred);
    },
  });

  /* -------------------------------------------------------------------- selection ---- */

  private readonly mode = signal<EditorMode>('edit');
  readonly editorMode = this.mode.asReadonly();

  /** Which node the tree has open. `linkedSignal` so a reload keeps it if it still exists. */
  readonly selectedCategoryId = linkedSignal<readonly CategoryNode[], number | null>({
    source: this.categories,
    computation: (categories, previous) => keepOrFirst(flatten(categories), previous?.value, (node) => node.id),
  });

  readonly selectedBrandId = linkedSignal<readonly BrandCard[], number | null>({
    source: this.brands,
    computation: (brands, previous) => keepOrFirst(brands, previous?.value, (brand) => brand.id),
  });

  readonly selectedTypeId = linkedSignal<readonly TypeCard[], number | null>({
    source: this.types,
    computation: (types, previous) => keepOrFirst(types, previous?.value, (type) => type.id),
  });

  readonly selectedGroupCode = linkedSignal<readonly GroupRow[], string | null>({
    source: this.groups,
    computation: (groups, previous) => keepOrFirst(groups, previous?.value, (group) => group.code),
  });

  /** Which branches are folded away. Collapsed rather than expanded, so a fresh tree opens open. */
  readonly collapsed = signal<ReadonlySet<number>>(new Set<number>());

  readonly selectedCategory = computed(() => {
    const id = this.selectedCategoryId();
    return id === null ? null : (find(flatten(this.categories()), (node) => node.id === id) ?? null);
  });

  readonly selectedBrand = computed(
    () => this.brands().find((brand) => brand.id === this.selectedBrandId()) ?? null,
  );

  readonly selectedType = computed(
    () => this.types().find((type) => type.id === this.selectedTypeId()) ?? null,
  );

  readonly selectedGroup = computed(
    () => this.groups().find((group) => group.code === this.selectedGroupCode()) ?? null,
  );

  /* ------------------------------------------------------------------- the drafts ---- */

  /**
   * Unsaved copy for the languages the editor is not currently showing.
   *
   * Keyed by language code and reset whenever the record under edit changes. The form is the truth
   * for the *active* language; this is the truth for the others.
   */
  private readonly draft = signal<ReadonlyMap<string, LocalisedCopy>>(new Map());

  /* ---------------------------------------------------------------------- heading ---- */

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('catalogue.heading.title'),
      context: this.transloco.translate('catalogue.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
      }),
    };
  });

  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const counts: Record<CatalogueTab, number> = {
      categories: this.categoryCount(),
      types: this.types().length,
      brands: this.brands().length,
      groups: this.groups().length,
    };
    return (['categories', 'types', 'brands', 'groups'] as const).map((tab) => ({
      key: tab,
      label: this.transloco.translate(`catalogue.tab.${tab}`),
      // No badge on a tab whose list failed: `0` would be a claim the console cannot make.
      badge: this.unavailable().includes(tab) ? undefined : String(counts[tab]),
    }));
  });

  /** What the header's primary action says, which depends on the tab it would add to. */
  readonly addLabel = computed(() => {
    this.transloco.activeLang();
    return this.transloco.translate(`catalogue.add.${this.activeTab()}`);
  });

  /* -------------------------------------------------------------------- the tree ---- */

  /**
   * The tree, as `shared/ui/tree` wants it.
   *
   * The count shown is `totalCount` — the branch's whole subtree — because "what is under here" is
   * the question an operator restructuring a hierarchy is asking. `productCount` alone would show
   * a parent with all its products in children as empty.
   */
  readonly treeNodes = computed<readonly TreeNode[]>(() => {
    const language = this.activeLanguage();
    const toTreeNode = (node: CategoryNode): TreeNode => ({
      id: node.id,
      label: copyFor(node.copy, language)?.name || node.name,
      meta: String(node.totalCount),
      visible: node.visible,
      children: node.children.map(toTreeNode),
    });
    return this.categories().map(toTreeNode);
  });

  /* --------------------------------------------------------------------- editing ---- */

  /**
   * Open a record for editing, discarding any unsaved draft on the previous one.
   *
   * Only moves the selection; the effect in the constructor is what loads the form, so this and the
   * first response arriving take exactly the same path.
   */
  select(tab: CatalogueTab, key: number | string): void {
    this.mode.set('edit');
    this.draft.set(new Map());
    switch (tab) {
      case 'categories':
        this.selectedCategoryId.set(key as number);
        break;
      case 'brands':
        this.selectedBrandId.set(key as number);
        break;
      case 'types':
        this.selectedTypeId.set(key as number);
        break;
      case 'groups':
        this.selectedGroupCode.set(key as string);
        break;
    }
  }

  /**
   * Start a new record on the active tab.
   *
   * `parentId` only means anything for a category, and it is how "add a child here" differs from
   * "add a top-level category" — the tree's row button passes one, the header's action does not.
   */
  startCreate(parentId?: number): void {
    this.mode.set('create');
    this.draft.set(new Map());
    this.newParentId = parentId ?? null;

    switch (this.activeTab()) {
      case 'categories':
        this.categoryForm.reset({code: '', visible: false, sortOrder: 0});
        this.categoryForm.controls.code.enable();
        break;
      case 'brands':
        this.brandForm.reset({code: ''});
        this.brandForm.controls.code.enable();
        break;
      case 'types':
        this.typeForm.reset({code: '', visible: true, allowAddToCart: true});
        this.typeForm.controls.code.enable();
        break;
      case 'groups':
        this.groupForm.reset({code: '', active: true});
        this.groupForm.controls.code.enable();
        break;
    }
  }

  /** Where a new category is being added. Null for a top-level one. */
  private newParentId: number | null = null;

  /** Abandon a create, or reload an edit from what the server last said. */
  cancelEdit(): void {
    this.draft.set(new Map());
    this.mode.set('edit');
    // Forces the effect to reload the form from what the server last said, discarding the draft.
    this.filledFor = null;
  }

  /**
   * Change which language the editor is writing.
   *
   * The current form value is parked in the draft first, so the language the operator is leaving
   * keeps whatever they had typed into it.
   */
  setLanguage(language: string): void {
    this.parkDraft();
    this.activeLanguage.set(language);
  }

  /**
   * The language being written, as a person reads it — "Arabic", not "ar".
   *
   * The editors' "not written in … yet" line is a sentence, and a bare locale code inside one reads
   * as a defect. Falls back to the code only for a language `Intl.DisplayNames` has no word for.
   */
  readonly activeLanguageName = computed(() => {
    const code = this.activeLanguage();
    return this.languages().find((language) => language.code === code)?.label ?? code;
  });

  /**
   * Which way the language being written runs.
   *
   * Not the console's direction — an operator reading the console in English still writes the
   * Arabic description right-to-left. `LocaleService` owns the mapping, so a language added there
   * needs no change here.
   */
  readonly activeLanguageDir = computed<'auto' | 'ltr' | 'rtl'>(() => {
    const code = this.activeLanguage();
    return this.locale.locales.find((entry) => entry.code === code)?.dir ?? 'auto';
  });

  /**
   * Which languages already have a name — the chip's "translated" mark.
   *
   * Read from what the server sent plus what has been parked in the draft, **not** from the live
   * form: a `FormControl`'s value is not a signal, so a `computed` over it would never re-run and
   * the mark would lag by one language switch. The chip for the language being edited is not marked
   * from here anyway — the editor's own "not translated" line answers that one, live.
   */
  /** Bumped on every keystroke in any tab's copy. See the constructor. */
  private readonly copyStamp = signal(0);

  /**
   * Which languages this record has a name in — stored, parked, or being typed right now.
   *
   * All three sources matter. The stored copy is what the server sent, the draft is what was typed
   * under another language and parked on the switch, and the form is the language on screen, which
   * reaches the draft only when the operator leaves it. Reading two of the three made a chip go grey
   * while its name was visibly in the field.
   */
  readonly translatedLanguages = computed<ReadonlySet<string>>(() => {
    this.copyStamp();
    const stored = new Map(this.currentStored().map((entry) => [entry.language, entry]));
    for (const [language, entry] of this.draft()) {
      stored.set(language, entry);
    }
    const live = this.formCopy();
    if (live) {
      stored.set(live.language, live);
    }
    return new Set(
      [...stored.values()].filter((entry) => entry.name.trim() !== '').map((entry) => entry.language),
    );
  });

  /**
   * The languages the store trades in that this record has no name in.
   *
   * A record saved without one is a record the storefront cannot render for those shoppers: the
   * populators replace the description list wholesale, so a language left blank is a language
   * cleared, not a language left alone. That is why this blocks the save rather than warning beside
   * it.
   */
  readonly missingLanguages = computed<readonly ReferenceOption[]>(() => {
    const written = this.translatedLanguages();
    return this.languages().filter((language) => !written.has(language.code));
  });

  readonly canSave = computed(() => this.missingLanguages().length === 0);

  /* ------------------------------------------------------------------- the writes ---- */

  saveCategory(): void {
    const form = this.categoryForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    if (!this.canSave()) {
      this.refuseIncomplete();
      return;
    }
    const copy = this.collectCopy();
    const {code, visible, sortOrder} = form.getRawValue();

    this.run(
      this.mode() === 'create'
        ? this.api.createCategory(copy, code, this.newParentId)
        : this.api.updateCategory(this.selectedCategoryId() ?? 0, copy, {visible, sortOrder}),
      'catalogue.saved.category',
      form,
    );
  }

  saveBrand(): void {
    const form = this.brandForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    if (!this.canSave()) {
      this.refuseIncomplete();
      return;
    }
    const copy = this.collectCopy();
    const {code} = form.getRawValue();

    this.run(
      this.mode() === 'create'
        ? this.api.createBrand(copy, code)
        : this.api.updateBrand(this.selectedBrandId() ?? 0, copy),
      'catalogue.saved.brand',
      form,
    );
  }

  saveType(): void {
    const form = this.typeForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    if (!this.canSave()) {
      this.refuseIncomplete();
      return;
    }
    const copy = this.collectCopy();
    const {code, visible, allowAddToCart} = form.getRawValue();

    this.run(
      this.mode() === 'create'
        ? this.api.createType(copy, code, {visible, allowAddToCart})
        : this.api.updateType(this.selectedTypeId() ?? 0, copy, {visible, allowAddToCart}),
      'catalogue.saved.type',
      form,
    );
  }

  saveGroup(): void {
    const form = this.groupForm;
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }
    if (!this.canSave()) {
      this.refuseIncomplete();
      return;
    }
    const copy = this.collectCopy();
    const {code, active} = form.getRawValue();
    this.run(this.api.saveGroup(code, copy, active), 'catalogue.saved.group', form);
  }

  /**
   * The eye toggle on a tree row.
   *
   * Its own endpoint rather than a save of the whole category, so toggling visibility does not
   * commit whatever half-finished copy is sitting in the editor beside it.
   */
  toggleCategoryVisible(node: TreeNode): void {
    this.run(this.api.setCategoryVisible(node.id, !node.visible), 'catalogue.saved.visibility');
  }

  setGroupActive(code: string, active: boolean): void {
    this.run(this.api.setGroupActive(code, active), 'catalogue.saved.group');
  }

  /**
   * A move from the tree, by drag or by the row's own controls — both emit the same event.
   *
   * Two intents, and both are `PUT …/category/{child}/move/{parent}` underneath:
   *
   * - `inside` nests the node into the target.
   * - `out` makes the node a sibling of the target, which is how it is promoted one level. The
   *   target is its current parent, so the new parent is the grandparent — or the root, which the
   *   pod spells `-1`.
   *
   * There is no sibling reordering. `sortOrder` is the only way to express it, the endpoint that
   * writes it is broken for every caller, and the hierarchy does not come back ordered by it
   * regardless. See lessons.md.
   */
  moveCategory(move: TreeMove): void {
    if (move.position === 'inside') {
      this.run(this.api.moveCategory(move.nodeId, move.targetId), 'catalogue.saved.moved');
      return;
    }

    const target = find(flatten(this.categories()), (candidate) => candidate.id === move.targetId);
    if (!target) {
      return;
    }
    this.run(this.api.moveCategory(move.nodeId, target.parentId), 'catalogue.saved.moved');
  }

  /** Promote a category out of its parent — the tree's "move out" button. */
  unnestCategory(nodeId: number): void {
    this.run(this.api.moveCategory(nodeId, null), 'catalogue.saved.moved');
  }

  toggleCollapsed(id: number): void {
    const next = new Set(this.collapsed());
    if (!next.delete(id)) {
      next.add(id);
    }
    this.collapsed.set(next);
  }

  expandAll(): void {
    this.collapsed.set(new Set());
  }

  collapseAll(): void {
    this.collapsed.set(new Set(flatten(this.categories()).map((node) => node.id)));
  }

  /* --------------------------------------------------------------- group members ---- */

  readonly memberResults = signal<readonly AutocompleteOption[]>([]);
  readonly memberSearching = signal(false);

  searchProducts(term: string): void {
    this.memberSearching.set(true);
    this.search.find(term, null).subscribe({
      next: (products) => {
        this.memberResults.set(
          products.map((product) => ({id: product.id, label: product.name, detail: product.sku})),
        );
        this.memberSearching.set(false);
      },
      error: () => {
        this.memberResults.set([]);
        this.memberSearching.set(false);
      },
    });
  }

  addMember(code: string, productId: number): void {
    this.memberResults.set([]);
    this.run(this.api.addGroupMember(code, productId), 'catalogue.saved.memberAdded');
  }

  removeMember(code: string, productId: number): void {
    this.run(this.api.removeGroupMember(code, productId), 'catalogue.saved.memberRemoved');
  }

  /* -------------------------------------------------------------------- deletion ---- */

  readonly pendingDelete = signal<PendingDelete | null>(null);

  askDelete(tab: CatalogueTab, key: number | string, name: string): void {
    this.pendingDelete.set({tab, key, name});
  }

  dismissDelete(): void {
    this.pendingDelete.set(null);
  }

  /**
   * Delete, once the dialog has been confirmed.
   *
   * Deleting a category takes its children with it — `CategoryFacadeImpl` cascades — which is why
   * the dialog names the record and why the confirm is not a bare "Are you sure".
   */
  confirmDelete(): void {
    const pending = this.pendingDelete();
    this.pendingDelete.set(null);
    if (!pending) {
      return;
    }
    const call =
      pending.tab === 'categories'
        ? this.api.deleteCategory(pending.key as number)
        : pending.tab === 'brands'
          ? this.api.deleteBrand(pending.key as number)
          : pending.tab === 'types'
            ? this.api.deleteType(pending.key as number)
            : this.api.deleteGroup(pending.key as string);

    this.run(call, 'catalogue.saved.deleted');
  }

  retry(): void {
    this.snapshot.reload();
  }

  /* --------------------------------------------------------------------- plumbing ---- */

  /**
   * One write, with the whole page's response to it.
   *
   * Every write answers a fresh snapshot rather than a status, so the success path is "replace what
   * is on screen with what the server now says". Field errors go back onto the form that caused
   * them; anything unmatched goes to a toast, because a validation failure with nothing marked is a
   * form that refuses to submit for no visible reason.
   */
  private run(call: Observable<CatalogueSnapshot>, messageKey: string, form?: AbstractControl): void {
    this.saving.set(true);
    call.subscribe({
      next: (snapshot) => {
        this.saving.set(false);
        this.loaded.set(snapshot);
        this.draft.set(new Map());
        this.mode.set('edit');
        this.filledFor = null;
        this.toast.success(this.transloco.translate(messageKey));
      },
      error: (failure: unknown) => {
        this.saving.set(false);
        if (form) {
          this.apiErrors.applyToForm(failure, form);
          return;
        }
        this.apiErrors.notify(failure);
      },
    });
  }

  /**
   * Says which languages are keeping the record from being saved.
   *
   * A toast rather than a field error, because the missing name is not in the field on screen — it
   * is in a language the operator has not opened. Naming them is the whole point; "fill in the
   * required fields" would send them hunting through a form that looks complete.
   */
  private refuseIncomplete(): void {
    const names = this.missingLanguages().map((language) => language.label).join(', ');
    this.toast.danger(this.transloco.translate('catalogue.missingLanguages', {languages: names}));
  }

  /** Moves the form's current language into the draft, so a language switch does not lose it. */
  private parkDraft(): void {
    const language = this.activeLanguage();
    const copy = this.formCopy();
    if (!copy) {
      return;
    }
    const next = new Map(this.draft());
    next.set(language, {...copy, language});
    this.draft.set(next);
  }

  /** The copy the active tab's form is holding, for the language on screen. */
  private formCopy(): LocalisedCopy | null {
    const form = this.activeForm();
    if (!form) {
      return null;
    }
    const value = form.controls.copy.getRawValue();
    return {
      language: this.activeLanguage(),
      name: value.name,
      friendlyUrl: value.friendlyUrl,
      description: value.description,
      title: value.title,
      metaDescription: value.metaDescription,
      // Neither is edited on this page; carried through so a save does not clear them.
      highlights: this.storedCopy(this.activeLanguage())?.highlights ?? '',
      keyWords: this.storedCopy(this.activeLanguage())?.keyWords ?? '',
    };
  }

  private activeForm() {
    switch (this.activeTab()) {
      case 'categories':
        return this.categoryForm;
      case 'brands':
        return this.brandForm;
      case 'types':
        return this.typeForm;
      case 'groups':
        return this.groupForm;
    }
  }

  /** What the selected record holds for a language, as the server last sent it. */
  private storedCopy(language: string): LocalisedCopy | undefined {
    return copyFor(this.currentStored(), language);
  }

  private currentStored(): readonly LocalisedCopy[] {
    if (this.mode() === 'create') {
      return [];
    }
    switch (this.activeTab()) {
      case 'categories':
        return this.selectedCategory()?.copy ?? [];
      case 'brands':
        return this.selectedBrand()?.copy ?? [];
      case 'types':
        return this.selectedType()?.copy ?? [];
      case 'groups':
        return this.selectedGroup()?.copy ?? [];
    }
  }

  /**
   * Every language's copy, ready to send.
   *
   * The active language comes from the form, the rest from the draft, and anything untouched from
   * what the server sent — a language left alone must go back exactly as it arrived, because these
   * endpoints replace the description list rather than merging it.
   */
  private collectCopy(): readonly LocalisedCopy[] {
    this.parkDraft();
    const merged = new Map(this.currentStored().map((entry) => [entry.language, entry]));
    for (const [language, entry] of this.draft()) {
      merged.set(language, entry);
    }
    return [...merged.values()].filter((entry) => entry.name.trim() !== '' || entry.description.trim() !== '');
  }

  /** Reload the active tab's form from the selected record. */
  private refill(): void {
    switch (this.activeTab()) {
      case 'categories':
        this.fillCategory();
        break;
      case 'brands':
        this.fillBrand();
        break;
      case 'types':
        this.fillType();
        break;
      case 'groups':
        this.fillGroup();
        break;
    }
  }

  private fillCategory(): void {
    const node = this.selectedCategory();
    if (!node) {
      return;
    }
    this.categoryForm.reset({code: node.code, visible: node.visible, sortOrder: node.sortOrder});
    // A code identifies the record; changing it is a different record, not an edit.
    this.categoryForm.controls.code.disable();
    this.fillCopy(this.categoryForm.controls.copy, node.copy);
  }

  private fillBrand(): void {
    const brand = this.selectedBrand();
    if (!brand) {
      return;
    }
    this.brandForm.reset({code: brand.code});
    this.brandForm.controls.code.disable();
    this.fillCopy(this.brandForm.controls.copy, brand.copy);
  }

  private fillType(): void {
    const type = this.selectedType();
    if (!type) {
      return;
    }
    this.typeForm.reset({code: type.code, visible: type.visible, allowAddToCart: type.allowAddToCart});
    this.typeForm.controls.code.disable();
    this.fillCopy(this.typeForm.controls.copy, type.copy);
  }

  private fillGroup(): void {
    const group = this.selectedGroup();
    if (!group) {
      return;
    }
    this.groupForm.reset({code: group.code, active: group.active});
    this.groupForm.controls.code.disable();
    this.fillCopy(this.groupForm.controls.copy, group.copy);
  }

  /** The draft wins over what the server sent — it is what the operator has typed and not yet saved. */
  private fillCopy(
    control: ReturnType<CatalogueFormService['copy']>,
    stored: readonly LocalisedCopy[],
  ): void {
    const language = this.activeLanguage();
    const copy = this.draft().get(language) ?? copyFor(stored, language) ?? emptyCopy(language);
    control.reset({
      name: copy.language === language ? copy.name : '',
      friendlyUrl: copy.language === language ? copy.friendlyUrl : '',
      description: copy.language === language ? copy.description : '',
      title: copy.language === language ? copy.title : '',
      metaDescription: copy.language === language ? copy.metaDescription : '',
    });
  }

  /** Offers a code derived from the name, while creating. Editable, and never overwrites a typed one. */
  suggestCode(tab: CatalogueTab, name: string): void {
    if (this.mode() !== 'create') {
      return;
    }
    const form =
      tab === 'categories'
        ? this.categoryForm
        : tab === 'brands'
          ? this.brandForm
          : tab === 'types'
            ? this.typeForm
            : this.groupForm;
    if (form.controls.code.dirty) {
      return;
    }
    const slug = slugify(name);
    if (slug) {
      form.controls.code.setValue(slug);
    }
  }
}

/* ---------------------------------------------------------------------------- helpers ---- */

/** Depth-first, in display order. */
function flatten(nodes: readonly CategoryNode[]): readonly CategoryNode[] {
  return nodes.flatMap((node) => [node, ...flatten(node.children)]);
}

function find<T>(items: readonly T[], predicate: (item: T) => boolean): T | undefined {
  return items.find(predicate);
}

/**
 * Keep the current selection if it survived the reload, otherwise fall to the first record.
 *
 * A save can delete the thing that was selected — deleting a category, or renaming a group's code,
 * which creates a different record — and an editor bound to a record that no longer exists renders
 * blank with no way back.
 */
function keepOrFirst<T, K>(items: readonly T[], previous: K | undefined, keyOf: (item: T) => K): K | null {
  if (previous !== undefined && previous !== null && items.some((item) => keyOf(item) === previous)) {
    return previous;
  }
  return items.length ? keyOf(items[0]) : null;
}
