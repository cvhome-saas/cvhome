/** Console-native; not a port from seller-core. */
import {Injectable, computed, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {catchError, map, of} from 'rxjs';

import {LayoutsService} from '@api/content/layouts.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import type {
  LayoutDocument,
  LayoutFieldError,
  LayoutItem,
  LayoutMeta,
  LayoutSection,
  ManifestPreset,
  SavedSection,
} from '@models/layout';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';

import {StorefrontOriginService} from '../services/storefront-origin.service';

export type BuilderDevice = 'desktop' | 'tablet' | 'mobile';

export type SaveState = 'idle' | 'pending' | 'saving' | 'saved' | 'conflict' | 'error';

const PAGE = 'HOME' as const;

const MAX_UNDO = 50;

const SAVE_DEBOUNCE_MS = 2_500;

const clone = <T>(value: T): T => structuredClone(value);

const newId = (prefix: string): string =>
  `${prefix}_${Date.now().toString(36)}${Math.random().toString(36).slice(2, 7)}`;

/**
 * The builder's whole state: the draft document in a signal, mutated only through {@link apply}, which
 * snapshots for undo and schedules the debounced save. The published copy is never held here — the canvas
 * iframe previews the draft through the storefront, the only honest renderer of it.
 */
@Injectable()
export class BuilderFacade {
  private readonly layouts = inject(LayoutsService);
  private readonly stores = inject(MerchantStoreService);
  private readonly origins = inject(StorefrontOriginService);
  private readonly shell = inject(ConsoleShellFacade);

  // ------------------------------------------------------------------------------------------ document

  readonly doc = signal<LayoutDocument | null>(null);
  readonly meta = signal<LayoutMeta | null>(null);
  readonly saveState = signal<SaveState>('idle');
  readonly publishWarnings = signal<LayoutFieldError[]>([]);

  private baseVersion = 1;
  private undoStack: LayoutDocument[] = [];
  private redoStack: LayoutDocument[] = [];
  private saveTimer: ReturnType<typeof setTimeout> | null = null;
  private saving = false;
  private readonly undoDepth = signal(0);
  private readonly redoDepth = signal(0);

  readonly canUndo = computed(() => this.undoDepth() > 0);
  readonly canRedo = computed(() => this.redoDepth() > 0);
  readonly dirty = computed(() => this.meta()?.dirty ?? false);

  // ----------------------------------------------------------------------------------------- selection

  readonly selectedId = signal<string | null>(null);
  readonly device = signal<BuilderDevice>('desktop');
  readonly lang = signal('en');

  readonly selected = computed<LayoutSection | null>(() => {
    const id = this.selectedId();
    return this.doc()?.sections.find((section) => section.id === id) ?? null;
  });

  // ----------------------------------------------------------------------------------- store languages

  private readonly localesResource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () =>
      this.stores.store().pipe(
        map((store) => {
          const codes = [...new Set([store.defaultLanguage ?? 'en', ...(store.supportedLanguages ?? [])])];
          return {codes, defaultCode: store.defaultLanguage ?? codes[0] ?? 'en'};
        }),
        catchError(() => of({codes: ['en'], defaultCode: 'en'})),
      ),
  });

  readonly locales = computed<{codes: string[]; defaultCode: string}>(() =>
    this.localesResource.hasValue() ? this.localesResource.value() : {codes: ['en'], defaultCode: 'en'},
  );

  // ------------------------------------------------------------------------------- manifest & presets

  /** The storefront origin + the active theme's capabilities; null while unknown or unreachable. */
  private readonly manifestResource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.origins.manifest().pipe(catchError(() => of(null))),
  });

  readonly manifest = computed(() =>
    this.manifestResource.hasValue() ? (this.manifestResource.value()?.manifest ?? null) : null,
  );

  readonly storefrontOrigin = computed(() =>
    this.manifestResource.hasValue() ? (this.manifestResource.value()?.origin ?? null) : null,
  );

  readonly kindSpec = (kind: string) => this.manifest()?.kinds.find((spec) => spec.kind === kind);

  private readonly savedSectionsResource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.layouts.sectionPresets().pipe(catchError(() => of([] as SavedSection[]))),
  });

  readonly savedSections = computed(() =>
    this.savedSectionsResource.hasValue() ? this.savedSectionsResource.value() : [],
  );

  // ------------------------------------------------------------------------------------------ preview

  /** Bumped on every successful save so the canvas iframe reloads the fresh draft. */
  readonly savedRevision = signal(0);
  readonly previewToken = signal<string | null>(null);

  // -------------------------------------------------------------------------------------------- load

  load(): void {
    this.saveState.set('idle');
    this.layouts.get(PAGE).subscribe({
      next: (layout) => {
        this.baseVersion = layout.meta.draftVersion;
        this.doc.set(layout.draft);
        this.meta.set(layout.meta);
        this.undoStack = [];
        this.redoStack = [];
        this.undoDepth.set(0);
        this.redoDepth.set(0);
        this.selectedId.set(layout.draft.sections[0]?.id ?? null);
        this.mintPreviewToken();
      },
      error: () => this.saveState.set('error'),
    });
    const defaultCode = this.locales().defaultCode;
    if (defaultCode) {
      this.lang.set(defaultCode);
    }
  }

  mintPreviewToken(): void {
    this.layouts.previewToken(PAGE).subscribe({
      next: ({token}) => this.previewToken.set(token),
      error: () => this.previewToken.set(null),
    });
  }

  // ---------------------------------------------------------------------------------------- mutation

  /** Every change goes through here: snapshot for undo, mutate a clone, mark dirty, schedule the save. */
  apply(mutate: (doc: LayoutDocument) => void): void {
    const current = this.doc();
    if (!current) {
      return;
    }
    this.undoStack.push(clone(current));
    if (this.undoStack.length > MAX_UNDO) {
      this.undoStack.shift();
    }
    this.redoStack = [];
    const next = clone(current);
    mutate(next);
    this.doc.set(next);
    this.undoDepth.set(this.undoStack.length);
    this.redoDepth.set(0);
    this.meta.update((meta) => (meta ? {...meta, dirty: true} : meta));
    this.scheduleSave();
  }

  undo(): void {
    const previous = this.undoStack.pop();
    const current = this.doc();
    if (!previous || !current) {
      return;
    }
    this.redoStack.push(clone(current));
    this.doc.set(previous);
    this.undoDepth.set(this.undoStack.length);
    this.redoDepth.set(this.redoStack.length);
    this.scheduleSave();
  }

  redo(): void {
    const next = this.redoStack.pop();
    const current = this.doc();
    if (!next || !current) {
      return;
    }
    this.undoStack.push(clone(current));
    this.doc.set(next);
    this.undoDepth.set(this.undoStack.length);
    this.redoDepth.set(this.redoStack.length);
    this.scheduleSave();
  }

  // ------------------------------------------------------------------------------------ section ops

  addFromPreset(preset: ManifestPreset, afterId?: string | null): void {
    const section: LayoutSection = {
      id: newId('sec'),
      kind: preset.kind,
      variant: preset.variant,
      props: clone(preset.props ?? {}),
      items: preset.items?.map((item) => ({
        id: newId('itm'),
        props: clone(item.props ?? {}),
        text: clone(item.text ?? {}),
      })),
      text: clone(preset.text ?? {}),
      style: clone(preset.style ?? {spacing: 'md', width: 'content', tone: 'default'}),
      visibility: {hidden: false},
      anchor: null,
    };
    this.insert(section, afterId);
  }

  addSavedSection(saved: SavedSection, afterId?: string | null): void {
    const section = clone(saved.section);
    section.id = newId('sec');
    section.items = section.items?.map((item) => ({...item, id: newId('itm')}));
    this.insert(section, afterId);
  }

  private insert(section: LayoutSection, afterId?: string | null): void {
    this.apply((doc) => {
      const at = afterId ? doc.sections.findIndex((candidate) => candidate.id === afterId) + 1 : doc.sections.length;
      doc.sections.splice(at || doc.sections.length, 0, section);
    });
    this.selectedId.set(section.id);
  }

  duplicate(id: string): void {
    let copyId: string | null = null;
    this.apply((doc) => {
      const index = doc.sections.findIndex((section) => section.id === id);
      if (index < 0) {
        return;
      }
      const copy = clone(doc.sections[index]);
      copy.id = newId('sec');
      copy.items = copy.items?.map((item) => ({...item, id: newId('itm')}));
      copy.anchor = null;
      doc.sections.splice(index + 1, 0, copy);
      copyId = copy.id;
    });
    if (copyId) {
      this.selectedId.set(copyId);
    }
  }

  remove(id: string): void {
    this.apply((doc) => {
      doc.sections = doc.sections.filter((section) => section.id !== id);
    });
    if (this.selectedId() === id) {
      this.selectedId.set(this.doc()?.sections[0]?.id ?? null);
    }
  }

  move(fromIndex: number, toIndex: number): void {
    this.apply((doc) => {
      const [section] = doc.sections.splice(fromIndex, 1);
      doc.sections.splice(toIndex, 0, section);
    });
  }

  toggleHidden(id: string): void {
    this.apply((doc) => {
      const section = doc.sections.find((candidate) => candidate.id === id);
      if (section) {
        section.visibility = {...(section.visibility ?? {}), hidden: !section.visibility?.hidden};
      }
    });
  }

  updateSelected(mutate: (section: LayoutSection) => void): void {
    const id = this.selectedId();
    if (!id) {
      return;
    }
    this.apply((doc) => {
      const section = doc.sections.find((candidate) => candidate.id === id);
      if (section) {
        mutate(section);
      }
    });
  }

  updateSelectedItem(itemId: string, mutate: (item: LayoutItem) => void): void {
    this.updateSelected((section) => {
      const item = section.items?.find((candidate) => candidate.id === itemId);
      if (item) {
        mutate(item);
      }
    });
  }

  addItem(): void {
    this.updateSelected((section) => {
      section.items = [...(section.items ?? []), {id: newId('itm'), props: {}, text: {}}];
    });
  }

  removeItem(itemId: string): void {
    this.updateSelected((section) => {
      section.items = (section.items ?? []).filter((item) => item.id !== itemId);
    });
  }

  moveItem(fromIndex: number, toIndex: number): void {
    this.updateSelected((section) => {
      const items = section.items ?? [];
      const [item] = items.splice(fromIndex, 1);
      items.splice(toIndex, 0, item);
      section.items = items;
    });
  }

  // ------------------------------------------------------------------------------------ persistence

  private scheduleSave(): void {
    this.saveState.set('pending');
    if (this.saveTimer) {
      clearTimeout(this.saveTimer);
    }
    this.saveTimer = setTimeout(() => this.saveNow(), SAVE_DEBOUNCE_MS);
  }

  /** Flushes the debounce; resolves when the draft is on the server (or the save failed). */
  saveNow(): Promise<boolean> {
    if (this.saveTimer) {
      clearTimeout(this.saveTimer);
      this.saveTimer = null;
    }
    const doc = this.doc();
    if (!doc || this.saveState() === 'idle' || this.saveState() === 'saved') {
      return Promise.resolve(true);
    }
    if (this.saving) {
      // a save is in flight; the pending state re-queues after it lands
      return Promise.resolve(false);
    }
    this.saving = true;
    this.saveState.set('saving');
    return new Promise((resolve) => {
      this.layouts.save(PAGE, doc, this.baseVersion).subscribe({
        next: (layout) => {
          this.saving = false;
          this.baseVersion = layout.meta.draftVersion;
          this.meta.set(layout.meta);
          this.saveState.set('saved');
          this.savedRevision.update((revision) => revision + 1);
          resolve(true);
        },
        error: (error: {status?: number}) => {
          this.saving = false;
          this.saveState.set(error?.status === 409 ? 'conflict' : 'error');
          resolve(false);
        },
      });
    });
  }

  async publish(): Promise<boolean> {
    const flushed = await this.saveNow();
    if (!flushed && this.saveState() !== 'saved') {
      return false;
    }
    return new Promise((resolve) => {
      this.layouts.publish(PAGE, this.baseVersion).subscribe({
        next: (published) => {
          this.meta.set(published.meta);
          this.publishWarnings.set(published.warnings ?? []);
          resolve(true);
        },
        error: (error: {status?: number}) => {
          this.saveState.set(error?.status === 409 ? 'conflict' : 'error');
          resolve(false);
        },
      });
    });
  }

  discard(): void {
    this.layouts.discard(PAGE, this.baseVersion).subscribe({
      next: (layout) => {
        this.baseVersion = layout.meta.draftVersion;
        this.doc.set(layout.draft);
        this.meta.set(layout.meta);
        this.undoStack = [];
        this.redoStack = [];
        this.undoDepth.set(0);
        this.redoDepth.set(0);
        this.saveState.set('idle');
        this.savedRevision.update((revision) => revision + 1);
        this.selectedId.set(layout.draft.sections[0]?.id ?? null);
      },
      error: () => this.saveState.set('error'),
    });
  }

  // --------------------------------------------------------------------------------- saved sections

  saveSelectedAsPreset(name: string): void {
    const section = this.selected();
    if (!section) {
      return;
    }
    this.layouts.saveSectionPreset(name, section).subscribe({
      next: () => this.savedSectionsResource.reload(),
    });
  }

  deleteSavedSection(id: number): void {
    this.layouts.deleteSectionPreset(id).subscribe({
      next: () => this.savedSectionsResource.reload(),
    });
  }
}
