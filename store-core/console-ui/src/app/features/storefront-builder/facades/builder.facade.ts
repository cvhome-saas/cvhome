/** Console-native; not a port from seller-core. */
import {DestroyRef, Injectable, computed, effect, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {catchError, map, of} from 'rxjs';

import {LayoutsService} from '@api/content/layouts.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import type {
  LayoutDocument,
  LayoutFieldError,
  LayoutItem,
  LayoutMeta,
  LayoutRevisionRow,
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

/** The preview token's server TTL is 30 minutes; re-mint with a comfortable margin. */
const TOKEN_REFRESH_MS = 20 * 60 * 1_000;

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
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  constructor() {
    // The store's languages land after load() has already run (the resource resolves asynchronously), so the
    // editing language follows them reactively — until the user picks one themselves.
    effect(() => {
      const defaultCode = this.locales().defaultCode;
      if (!this.langTouched() && defaultCode) {
        this.lang.set(defaultCode);
      }
    });
    inject(DestroyRef).onDestroy(() => {
      this.cancelPendingSave();
      if (this.tokenTimer) {
        clearTimeout(this.tokenTimer);
      }
    });
  }

  // ------------------------------------------------------------------------------------------ document

  readonly doc = signal<LayoutDocument | null>(null);
  readonly meta = signal<LayoutMeta | null>(null);
  readonly saveState = signal<SaveState>('idle');
  readonly publishWarnings = signal<LayoutFieldError[]>([]);

  private baseVersion = 1;
  private undoStack: LayoutDocument[] = [];
  private redoStack: LayoutDocument[] = [];
  private saveTimer: ReturnType<typeof setTimeout> | null = null;
  private tokenTimer: ReturnType<typeof setTimeout> | null = null;
  private saving = false;
  /** The in-flight save, so a flush during one chains behind it instead of dead-dropping. */
  private inFlight: Promise<boolean> | null = null;
  /**
   * Bumped by every operation that replaces the document wholesale (load, discard, restore). A save that was
   * already in flight when the document changed under it must not write its stale result back.
   */
  private generation = 0;
  /** True after a failed initial GET — retry must re-load, not re-save nothing. */
  readonly loadFailed = signal(false);
  private readonly langTouched = signal(false);
  private readonly undoDepth = signal(0);
  private readonly redoDepth = signal(0);

  readonly canUndo = computed(() => this.undoDepth() > 0);
  readonly canRedo = computed(() => this.redoDepth() > 0);
  readonly dirty = computed(() => this.meta()?.dirty ?? false);
  /** Edits not yet on the server. Distinct from {@link dirty}, which means draft ≠ published. */
  readonly unsaved = computed(() => this.saveState() === 'pending' || this.saveState() === 'saving');

  // ----------------------------------------------------------------------------------------- selection

  readonly selectedId = signal<string | null>(null);
  /** Layer-row ⇄ canvas hover mirror. */
  readonly hoveredId = signal<string | null>(null);
  readonly device = signal<BuilderDevice>('desktop');
  readonly lang = signal('en');
  /** Canvas guides: outline + tag every section, show the add-here zones. */
  readonly guides = signal(false);
  /** A library drag in flight (label travels to the bridge); null otherwise. */
  readonly dragging = signal<{kind: string; label: string} | null>(null);
  /** What the in-flight drag would insert; consumed by {@link dropDraggedAt}. */
  private pendingDrag: {preset?: ManifestPreset; saved?: SavedSection} | null = null;
  /** Registered by the page so canvas add-here intents can open the library panel. */
  openLibrary?: () => void;
  /** The bridge's last-reported insertion point during a drag (undefined = no report yet). */
  readonly dropBeforeId = signal<string | null | undefined>(undefined);
  /** Where the next library insert lands (from an add-here zone); undefined = after the selection. */
  readonly insertTarget = signal<string | null | undefined>(undefined);

  readonly lockedIds = computed<string[]>(() =>
    (this.doc()?.sections ?? []).filter((section) => section.locked).map((section) => section.id));

  /** Every mutation is refused while a conflict stands — reload is the only way forward. */
  readonly frozen = computed(() => this.saveState() === 'conflict');

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

  /**
   * Loads (or reloads) the draft. The selection survives when the preferred section still exists —
   * by default the current one, so a conflict reload or retry lands the merchant back on the block
   * they were editing; the page passes the URL fragment on first mount for the same continuity
   * across a browser refresh.
   */
  load(preferredId: string | null = this.selectedId()): void {
    this.cancelPendingSave();
    this.generation += 1;
    this.saveState.set('idle');
    this.layouts.get(PAGE).subscribe({
      next: (layout) => {
        this.loadFailed.set(false);
        this.baseVersion = layout.meta.draftVersion;
        this.doc.set(layout.draft);
        this.meta.set(layout.meta);
        this.undoStack = [];
        this.redoStack = [];
        this.undoDepth.set(0);
        this.redoDepth.set(0);
        const kept = preferredId && layout.draft.sections.some((section) => section.id === preferredId)
          ? preferredId : layout.draft.sections[0]?.id ?? null;
        this.selectedId.set(kept);
        this.mintPreviewToken();
      },
      error: () => {
        this.loadFailed.set(true);
        this.saveState.set('error');
      },
    });
  }

  /** The error bar's one button: a failed load re-loads, anything else flushes the save. */
  retry(): void {
    if (this.loadFailed() || !this.doc()) {
      this.load();
    } else {
      void this.saveNow();
    }
  }

  /** The user's own language pick wins over the store default from then on. */
  pickLang(code: string): void {
    this.langTouched.set(true);
    this.lang.set(code);
  }

  mintPreviewToken(): void {
    this.layouts.previewToken(PAGE).subscribe({
      next: ({token}) => {
        this.previewToken.set(token);
        // the token lives 30 minutes; a fresh one before that keeps a long editing session's canvas
        // and preview tab on the draft instead of silently falling back to the published page
        if (this.tokenTimer) {
          clearTimeout(this.tokenTimer);
        }
        this.tokenTimer = setTimeout(() => this.mintPreviewToken(), TOKEN_REFRESH_MS);
      },
      error: () => this.previewToken.set(null),
    });
  }

  // ---------------------------------------------------------------------------------------- mutation

  /** Every change goes through here: snapshot for undo, mutate a clone, mark dirty, schedule the save. */
  apply(mutate: (doc: LayoutDocument) => void): void {
    const current = this.doc();
    if (!current || this.frozen()) {
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
    if (this.frozen()) {
      return; // a conflict stands; mutating (and re-saving) would only 409 again
    }
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
    if (this.frozen()) {
      return;
    }
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

  /**
   * The add-here / drop target wins when one is set; `undefined` keeps the default (after the
   * selection); a `null` target appends.
   */
  private insert(section: LayoutSection, afterId?: string | null): void {
    const target = this.insertTarget();
    this.insertTarget.set(undefined);
    this.apply((doc) => {
      let at: number;
      if (target !== undefined) {
        // a boundary that no longer exists (undone away mid-drag) appends rather than jumping to the top
        const index = target === null ? -1 : doc.sections.findIndex((candidate) => candidate.id === target);
        at = index < 0 ? doc.sections.length : index;
      } else {
        const anchor = afterId ? doc.sections.findIndex((candidate) => candidate.id === afterId) + 1 : 0;
        at = anchor > 0 ? anchor : doc.sections.length;
      }
      doc.sections.splice(at, 0, section);
    });
    this.selectedId.set(section.id);
  }

  /** A canvas drop: insert at the bridge-reported boundary. */
  insertAt(target: string | null | undefined): void {
    this.insertTarget.set(target);
  }

  // -------------------------------------------------------------------------------- library drags

  startDrag(payload: {preset?: ManifestPreset; saved?: SavedSection}, kind: string, label: string): void {
    this.pendingDrag = payload;
    this.dropBeforeId.set(undefined);
    this.dragging.set({kind, label});
  }

  /** dragend fires on drop, Esc and drop-outside alike — the single disarm path. */
  endDrag(): void {
    this.pendingDrag = null;
    this.dragging.set(null);
    this.dropBeforeId.set(undefined);
  }

  /** Set by a landed drop and consumed once by the canvas, which holds the gap open until reload. */
  private droppedBefore: string | null | undefined = undefined;

  /** One-shot read of the just-landed drop's boundary; undefined when nothing dropped. */
  takeDropped(): string | null | undefined {
    const value = this.droppedBefore;
    this.droppedBefore = undefined;
    return value;
  }

  /** The canvas drop: insert the pending drag's section at the bridge-reported boundary. */
  dropDraggedAt(beforeId: string | null | undefined): void {
    const payload = this.pendingDrag;
    if (!payload || beforeId === undefined) {
      return;
    }
    this.droppedBefore = beforeId;
    this.insertTarget.set(beforeId);
    if (payload.preset) {
      this.addFromPreset(payload.preset);
    } else if (payload.saved) {
      this.addSavedSection(payload.saved);
    }
  }

  moveById(id: string, delta: number): void {
    const doc = this.doc();
    if (!doc || this.isLocked(id)) {
      return;
    }
    const from = doc.sections.findIndex((section) => section.id === id);
    const to = from + delta;
    if (from < 0 || to < 0 || to >= doc.sections.length) {
      return;
    }
    this.move(from, to);
  }

  /** The bridge's `reorder {sectionId, beforeId}`: move the section in front of the boundary. */
  reorderBefore(id: string, beforeId: string | null): void {
    if (this.isLocked(id)) {
      return;
    }
    this.apply((doc) => {
      const from = doc.sections.findIndex((section) => section.id === id);
      if (from < 0) {
        return;
      }
      const [section] = doc.sections.splice(from, 1);
      const index = beforeId === null ? -1 : doc.sections.findIndex((candidate) => candidate.id === beforeId);
      const at = index < 0 ? doc.sections.length : index;
      doc.sections.splice(at, 0, section);
    });
  }

  toggleLocked(id: string): void {
    this.apply((doc) => {
      const section = doc.sections.find((candidate) => candidate.id === id);
      if (section) {
        section.locked = !section.locked;
      }
    });
  }

  duplicate(id: string): void {
    if (this.isLocked(id)) {
      return;
    }
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
    if (this.isLocked(id)) {
      return;
    }
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

  isLocked(id: string): boolean {
    return this.doc()?.sections.find((section) => section.id === id)?.locked === true;
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

  /** Forgets the scheduled save; every operation that replaces the document calls this first. */
  private cancelPendingSave(): void {
    if (this.saveTimer) {
      clearTimeout(this.saveTimer);
      this.saveTimer = null;
    }
  }

  private scheduleSave(): void {
    if (this.frozen()) {
      return; // never let a mutation's 'pending' erase the conflict banner
    }
    this.saveState.set('pending');
    this.cancelPendingSave();
    this.saveTimer = setTimeout(() => this.saveNow(), SAVE_DEBOUNCE_MS);
  }

  /** Flushes the debounce; resolves when the draft is on the server (or the save failed). */
  saveNow(): Promise<boolean> {
    this.cancelPendingSave();
    const doc = this.doc();
    if (!doc || this.saveState() === 'idle' || this.saveState() === 'saved') {
      return Promise.resolve(true);
    }
    if (this.saving && this.inFlight) {
      // chain behind the in-flight save, then flush whatever is still pending
      return this.inFlight.then(() => this.saveNow());
    }
    const generation = this.generation;
    this.saving = true;
    this.saveState.set('saving');
    this.inFlight = new Promise((resolve) => {
      this.layouts.save(PAGE, doc, this.baseVersion).subscribe({
        next: (layout) => {
          this.saving = false;
          if (generation !== this.generation) {
            resolve(false); // the document was replaced while this save flew; its result is history
            return;
          }
          this.baseVersion = layout.meta.draftVersion;
          this.meta.set(layout.meta);
          this.saveState.set('saved');
          this.savedRevision.update((revision) => revision + 1);
          resolve(true);
        },
        error: (error: {status?: number}) => {
          this.saving = false;
          if (generation !== this.generation) {
            resolve(false);
            return;
          }
          this.saveState.set(error?.status === 409 ? 'conflict' : 'error');
          resolve(false);
        },
      });
    });
    return this.inFlight;
  }

  async publish(): Promise<boolean> {
    const flushed = await this.saveNow();
    if (!flushed && this.saveState() !== 'saved') {
      this.toast.danger(this.transloco.translate('builder.toast.publishFailed'));
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
          this.toast.danger(this.transloco.translate('builder.toast.publishFailed'));
          resolve(false);
        },
      });
    });
  }

  discard(): void {
    this.cancelPendingSave();
    this.generation += 1;
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

  // ------------------------------------------------------------------------------------- revisions

  /** Loaded on demand when the history drawer opens; null = not yet asked. */
  readonly revisions = signal<LayoutRevisionRow[] | null>(null);

  loadRevisions(): void {
    this.layouts.revisions(PAGE).subscribe({
      next: (rows) => this.revisions.set(rows),
      error: () => this.revisions.set([]),
    });
  }

  /** Restores a published version into the draft; publishing it again stays an explicit step. */
  restoreRevision(version: number): void {
    this.cancelPendingSave();
    this.generation += 1;
    this.layouts.restore(PAGE, version).subscribe({
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

  /** For the layer list's warning dots: the publish warning aimed at this section, if any. */
  warningFor(id: string): string | null {
    return this.publishWarnings().find((warning) => warning.field === id)?.message ?? null;
  }

  // --------------------------------------------------------------------------------- saved sections

  saveSelectedAsPreset(name: string): void {
    const section = this.selected();
    if (!section) {
      return;
    }
    this.layouts.saveSectionPreset(name, section).subscribe({
      next: () => this.savedSectionsResource.reload(),
      error: () => this.toast.danger(this.transloco.translate('builder.toast.presetSaveFailed')),
    });
  }

  deleteSavedSection(id: number): void {
    this.layouts.deleteSectionPreset(id).subscribe({
      next: () => this.savedSectionsResource.reload(),
      error: () => this.toast.danger(this.transloco.translate('builder.toast.presetDeleteFailed')),
    });
  }
}
