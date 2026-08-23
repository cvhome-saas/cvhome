import {Injectable, computed, inject, signal} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';
import {merge, of, startWith, switchMap} from 'rxjs';

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
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);
  private readonly cache = inject(ContentCache);
  private readonly hub = inject(ContentHubFacade);

  private type!: ContentListType;
  private extra: FormGroup | null = null;
  /** Copies what the editor reads but the form does not carry (status, locales…). */
  private populateExtra: ((item: R) => void) | null = null;

  readonly id = signal<number | null>(null);
  readonly item = signal<R | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly loadError = signal<Error | null>(null);

  common!: CommonForm;
  translations = signal<Readonly<Record<string, TranslationForm>>>({});

  /** The language being edited. */
  readonly language = signal('en');

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
  init(type: ContentListType, id: number | null, extra: FormGroup, populateExtra: (item: R) => void): void {
    this.type = type;
    this.extra = extra;
    this.populateExtra = populateExtra;
    this.common = this.forms.common(type, () => this.id());
    this.id.set(id);
    this.language.set(this.hub.locales().defaultCode);
    this.translations.set(this.forms.translations(this.hub.locales().codes));
    if (id !== null) {
      this.load(id);
    }
  }

  /** Called when the store's languages arrive after init (they are a resource). */
  syncLanguages(codes: readonly string[], defaultCode: string): void {
    this.translations.update((current) => this.forms.translations(codes, current));
    if (!codes.includes(this.language())) {
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
    this.api.get<R>(this.type, id).subscribe({
      next: (item) => {
        this.loading.set(false);
        this.apply(item);
      },
      error: (failure: unknown) => {
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
  }

  readonly dirty = computed(() => {
    this.formValue();
    return (
      this.common?.dirty || this.extra?.dirty || Object.values(this.translations()).some((f) => f.dirty) || false
    );
  });

  readonly invalid = computed(() => {
    this.formValue();
    return (
      this.common?.invalid || this.extra?.invalid || Object.values(this.translations()).some((f) => f.invalid) || false
    );
  });

  /** The request body: common + translations + the editor's own fields. */
  body(): P {
    const existing = this.item()?.translations ?? [];
    return {
      ...(this.extra?.getRawValue() ?? {}),
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
    if (this.saving()) {
      return;
    }
    this.saving.set(true);
    const body = this.body();
    const id = this.id();
    const request = id === null ? this.api.create(this.type, body) : this.api.update(this.type, id, body);
    request.subscribe({
      next: (saved) => {
        this.saving.set(false);
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
        this.saving.set(false);
        if (failure instanceof ApiError && failure.code === 'CONTENT.VERSION.CONFLICT') {
          this.toast.warning(this.transloco.translate('content.editor.versionConflict'));
          this.reload();
          return;
        }
        this.apiErrors.notify(failure);
      },
    });
  }

  /** Saves first (the transition should apply to what is on screen), then moves the status. */
  transition(action: TransitionAction, publishAt: string | null = null): void {
    const run = (id: number) => {
      this.saving.set(true);
      this.api.transition(this.type, id, action, publishAt ? {publishAt} : null).subscribe({
        next: (saved) => {
          this.saving.set(false);
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
          this.saving.set(false);
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
    this.saving.set(true);
    this.api.delete(this.type, id, true).subscribe({
      next: () => {
        this.saving.set(false);
        this.cache.invalidate();
        this.toast.success(this.transloco.translate('content.toast.deleted', {title: this.title()}));
        this.router.navigate(['/content', this.type]);
      },
      error: (failure: unknown) => {
        this.saving.set(false);
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
