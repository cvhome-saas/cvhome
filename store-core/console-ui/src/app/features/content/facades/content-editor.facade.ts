import {DOCUMENT} from '@angular/common';
import {Injectable, computed, inject, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {Subscription, merge, of, startWith, switchMap} from 'rxjs';

import {ContentCache} from '@api/content/content-cache';
import {ContentItemsService} from '@api/content/content-items.service';
import {ApiError} from '@core/errors/api-error';
import {ApiErrorService} from '@core/errors/api-error.service';
import type {
  ContentListType,
  ContentStatus,
  ContentTranslation,
  PersistableContent,
  ReadableContentMeta,
  TransitionAction,
} from '@models/content';
import {ToastService} from '@shared/ui/toast/toast';
import {
  ContentEditorFormService,
  type CommonForm,
  type TranslationForm,
} from '../services/content-editor-form.service';
import {ContentHubFacade} from './content-hub.facade';

/**
 * Everything a content editor does that does not depend on its type: load by id or start blank,
 * hold the common and per-locale forms, save (create or update, with the optimistic version),
 * run a status transition, delete, and keep the hub's counts current.
 *
 * Provided per editor component. The editor adds its own `extra` form group; its raw value is
 * spread into the request body, so a type needs no facade of its own.
 */
@Injectable()
export class ContentEditorFacade<P extends PersistableContent, R extends P & ReadableContentMeta> {
  private readonly api = inject(ContentItemsService);
  private readonly forms = inject(ContentEditorFormService);
  private readonly document = inject(DOCUMENT);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);
  private readonly cache = inject(ContentCache);
  private readonly hub = inject(ContentHubFacade);

  private type!: ContentListType;
  private extra: FormGroup | null = null;
  /** Reshapes the editor's flat form into the wire shape (banners nest target/artwork). */
  private bodyMapper: (() => Partial<P>) | null = null;
  /** Copies what the editor reads but the form does not carry (status, locales…). */
  private populateExtra: ((item: R) => void) | null = null;

  readonly id = signal<number | null>(null);
  readonly item = signal<R | null>(null);
  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly loadError = signal<Error | null>(null);

  common!: CommonForm;
  translations = signal<Readonly<Record<string, TranslationForm>>>({});

  /** The language being edited. */
  readonly language = signal('en');

  /**
   * Whether the shown language is the reader's own pick rather than the fallback `init` starts on.
   * The store's languages arrive as a resource, so an editor opened before they land would otherwise
   * keep the 'en' placeholder even when the store writes its source copy in another language.
   */
  private languageChosen = false;

  /** Guards against an older load's response overwriting a newer one. */
  private loadSeq = 0;

  /** Bumped on every change of the common and extra forms, so `dirty`/`invalid` re-evaluate. */
  private readonly formTick = signal(0);
  private formSubscriptions = new Subscription();

  readonly isNew = computed(() => this.id() === null);
  readonly status = computed<ContentStatus | null>(() => this.item()?.status ?? null);
  readonly version = computed(() => this.item()?.version ?? null);

  /** The store's languages, and the editor's active one once they are known. */
  readonly localeOptions = this.hub.localeOptions;

  private readonly formValue = toSignal(
    toObservable(this.translations).pipe(
      switchMap((groups) => {
        const all = Object.values(groups);
        return all.length
          ? merge(...all.map((g) => g.valueChanges.pipe(startWith(g.getRawValue()))))
          : of(null);
      }),
    ),
    {initialValue: null},
  );

  /** Languages that already carry a title — the switcher marks the rest as missing. */
  readonly written = computed<ReadonlySet<string>>(() => {
    this.formValue();
    return new Set(
      Object.entries(this.translations())
        .filter(([, form]) => form.controls.title.value.trim().length > 0)
        .map(([code]) => code),
    );
  });

  /**
   * Wires the facade to one editor: the type, the id from the route (null for `new`), the editor's
   * own form group and how to pour a loaded item into it.
   */
  init(
    type: ContentListType,
    id: number | null,
    extra: FormGroup,
    populateExtra: (item: R) => void,
  ): void {
    this.type = type;
    this.extra = extra;
    this.populateExtra = populateExtra;
    this.common = this.forms.common(type, () => this.id());
    this.formSubscriptions.unsubscribe();
    this.formSubscriptions = new Subscription();
    for (const form of [this.common, extra]) {
      this.formSubscriptions.add(
        form.valueChanges.subscribe(() => this.formTick.update((v) => v + 1)),
      );
      this.formSubscriptions.add(
        form.statusChanges.subscribe(() => this.formTick.update((v) => v + 1)),
      );
    }
    this.id.set(id);
    this.languageChosen = false;
    this.language.set(this.hub.locales().defaultCode);
    this.translations.set(this.forms.translations(this.hub.locales().codes));
    if (id !== null) {
      this.load(id);
    }
  }

  /** Records that the reader picked this language, so arriving store locales leave it alone. */
  chooseLanguage(code: string): void {
    this.languageChosen = true;
    this.language.set(code);
  }

  /** Installs a mapper whose result is spread over the raw extra form when building the body. */
  setBodyMapper(mapper: () => Partial<P>): void {
    this.bodyMapper = mapper;
  }

  /** Called when the store's languages arrive after init (they are a resource). */
  syncLanguages(codes: readonly string[], defaultCode: string): void {
    this.translations.update((current) => this.forms.translations(codes, current));
    if (!this.languageChosen || !codes.includes(this.language())) {
      this.language.set(defaultCode);
    }
    const loaded = this.item();
    if (loaded) {
      this.forms.fillTranslations(this.translations(), loaded.translations);
    }
  }

  load(id: number): void {
    this.loading.set(true);
    this.loadError.set(null);
    const seq = ++this.loadSeq;
    this.api.get<R>(this.type, id).subscribe({
      next: (item) => {
        // Publishing a new item loads twice — once after the create, once after the transition — and the
        // first response can arrive last, which used to leave a published item wearing its DRAFT badge.
        if (seq !== this.loadSeq) {
          return;
        }
        this.loading.set(false);
        this.apply(item);
      },
      error: (failure: unknown) => {
        if (seq !== this.loadSeq) {
          return;
        }
        this.loading.set(false);
        this.loadError.set(failure instanceof Error ? failure : new Error(String(failure)));
      },
    });
  }

  reload(): void {
    const id = this.id();
    if (id !== null) {
      this.load(id);
    }
  }

  private apply(item: R): void {
    this.item.set(item);
    this.forms.fillCommon(this.common, item);
    this.forms.fillTranslations(this.translations(), item.translations);
    this.populateExtra?.(item);
    this.common.markAsPristine();
    Object.values(this.translations()).forEach((f) => f.markAsPristine());
    this.extra?.markAsPristine();
    this.formTick.update((v) => v + 1);
  }

  readonly dirty = computed(() => {
    this.formValue();
    this.formTick();
    return (
      this.common?.dirty ||
      this.extra?.dirty ||
      Object.values(this.translations()).some((f) => f.dirty) ||
      false
    );
  });

  /**
   * Whether the slug is set and not known to be taken. A PENDING availability check counts as
   * fine — the checklist should not flash red on every keystroke while the server is consulted.
   */
  readonly slugOk = computed(() => {
    this.formValue();
    this.formTick();
    const slug = this.common?.controls.slug;
    return !!slug && slug.value.trim().length > 0 && !slug.invalid;
  });

  readonly invalid = computed(() => {
    this.formValue();
    this.formTick();
    return (
      this.common?.invalid ||
      this.extra?.invalid ||
      Object.values(this.translations()).some((f) => f.invalid) ||
      false
    );
  });

  /** The request body: common + translations + the editor's own fields. */
  body(): P {
    const existing = this.item()?.translations ?? [];
    return {
      ...(this.bodyMapper ? this.bodyMapper() : (this.extra?.getRawValue() ?? {})),
      ...this.forms.toCommon(this.common),
      translations: this.forms.toTranslations(this.translations(), existing),
      id: this.id() ?? undefined,
      version: this.version() ?? undefined,
    } as P;
  }

  /**
   * Saves as it is (a draft stays a draft, a published item stays published). A new item navigates
   * to its own route so a reload finds it. `then` runs after a successful save with the id.
   */
  save(then?: (id: number) => void): void {
    if (this.busy() || this.revealInvalid()) {
      return;
    }
    this.busy.set(true);
    const body = this.body();
    const id = this.id();
    const request =
      id === null ? this.api.create(this.type, body) : this.api.update(this.type, id, body);
    request.subscribe({
      next: (saved) => {
        this.busy.set(false);
        this.cache.invalidate();
        this.toast.success(this.transloco.translate('content.editor.saved'));
        if (id === null) {
          this.id.set(saved.id);
          this.router.navigate(['/content', this.type, saved.id], {replaceUrl: true});
        }
        this.load(saved.id);
        then?.(saved.id);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        if (failure instanceof ApiError && failure.code === 'CONTENT.VERSION.CONFLICT') {
          this.toast.warning(this.transloco.translate('content.editor.versionConflict'));
          this.reload();
          return;
        }
        this.apiErrors.notify(failure);
      },
    });
  }

  /**
   * Shows what is stopping a save, and says whether anything is.
   *
   * A disabled Save button was a dead end: the field it waits on — a slug, usually — sits far below the fold,
   * so the seller saw a button that did nothing and no reason why. Every control is marked touched (which is
   * what makes `app-form-field` render its error), the first offending one is scrolled to and focused, and a
   * toast names the situation.
   */
  private revealInvalid(): boolean {
    if (!this.invalid()) {
      return false;
    }
    for (const form of [this.common, this.extra, ...Object.values(this.translations())]) {
      form?.markAllAsTouched();
    }
    this.formTick.update((v) => v + 1);
    const field = this.document.querySelector<HTMLElement>(
      '.ng-invalid[formControlName], .ng-invalid > input, .ng-invalid > textarea, input.ng-invalid, textarea.ng-invalid',
    );
    field?.scrollIntoView({block: 'center', behavior: 'smooth'});
    field?.focus({preventScroll: true});
    this.toast.warning(this.transloco.translate('content.editor.checkFields'));
    return true;
  }

  /** Saves first (the transition should apply to what is on screen), then moves the status. */
  transition(action: TransitionAction, publishAt: string | null = null): void {
    if (this.revealInvalid()) {
      return;
    }
    const run = (id: number) => {
      this.busy.set(true);
      this.api.transition(this.type, id, action, publishAt ? {publishAt} : null).subscribe({
        next: (saved) => {
          this.busy.set(false);
          this.cache.invalidate();
          this.toast.success(
            this.transloco.translate(`content.toast.${action}`, {
              title: this.title(),
              status: this.transloco.translate(`content.status.${saved.status}`),
            }),
          );
          this.load(id);
        },
        error: (failure: unknown) => {
          this.busy.set(false);
          this.apiErrors.notify(failure);
        },
      });
    };
    if (this.dirty() || this.id() === null) {
      this.save(run);
    } else {
      run(this.id() as number);
    }
  }

  delete(): void {
    const id = this.id();
    if (id === null) {
      return;
    }
    this.busy.set(true);
    this.api.delete(this.type, id, true).subscribe({
      next: () => {
        this.busy.set(false);
        this.cache.invalidate();
        this.toast.success(
          this.transloco.translate('content.toast.deleted', {title: this.title()}),
        );
        this.router.navigate(['/content', this.type]);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** The title in the active language, else the first written one, else the slug. */
  title(): string {
    const forms = this.translations();
    const active = forms[this.language()]?.controls.title.value.trim();
    if (active) {
      return active;
    }
    for (const form of Object.values(forms)) {
      const value = form.controls.title.value.trim();
      if (value) {
        return value;
      }
    }
    return this.common?.controls.slug.value ?? '';
  }

  /**
   * The locale the publish gate will judge: the default one when it has a title, else the first
   * written one — the same fallback the server applies.
   */
  sourceTranslation(defaultCode: string): TranslationForm | undefined {
    const preferred = this.translations()[defaultCode];
    if (preferred && preferred.controls.title.value.trim()) {
      return preferred;
    }
    const written = this.written();
    for (const code of written) {
      const form = this.translations()[code];
      if (form) {
        return form;
      }
    }
    return preferred;
  }

  translationFor(code: string): TranslationForm | undefined {
    return this.translations()[code];
  }

  existingTranslation(code: string): ContentTranslation | undefined {
    return this.item()?.translations.find((t) => t.language === code);
  }

  back(): void {
    this.router.navigate(['/content', this.type]);
  }
}
