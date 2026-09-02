/** Console-native; not a port from seller-core. */
import {
  ChangeDetectionStrategy, Component, HostListener, type OnDestroy, type OnInit, computed, effect, inject,
  signal,
} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import type {ConfirmsLeave} from '@cvhome-saas/ui-kit';
import {Icon} from '@cvhome-saas/ui-kit/ui';
import type {IconName} from '@cvhome-saas/ui-kit/ui';

import {BuilderFacade, type BuilderDevice} from './facades/builder.facade';
import {BuilderInspector} from './components/inspector';
import {BuilderLayerList} from './components/layer-list';
import {BuilderPreviewFrame} from './components/preview-frame';
import {BuilderSectionLibrary} from './components/section-library';

/**
 * The storefront builder — the three-pane editor the `Storefront Builder.dc.html` design describes.
 * Left: the page's sections (Layout) and the Add-section library. Center: the real storefront rendering
 * the draft in an iframe. Right: the inspector, generated from the theme manifest's field DSL.
 *
 * A route of its own under store-management rather than a section pane: it needs the whole viewport,
 * and the design's top bar replaces the console shell's header height with its own chrome.
 */
@Component({
  selector: 'app-storefront-builder',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [BuilderFacade],
  imports: [
    BuilderInspector, BuilderLayerList, BuilderPreviewFrame, BuilderSectionLibrary, Icon, RouterLink,
    TranslocoDatePipe, TranslocoDirective,
  ],
  templateUrl: './storefront-builder.html',
  styleUrl: './storefront-builder.css',
})
export class StorefrontBuilder implements ConfirmsLeave, OnInit, OnDestroy {
  protected readonly facade = inject(BuilderFacade);
  private readonly transloco = inject(TranslocoService);

  /**
   * Only edits not yet on the server ask before the route is left — an unpublished draft is fine to walk
   * away from, the autosave holds it. `dirty` (draft ≠ published) is deliberately not consulted here.
   */
  canLeave(): boolean {
    return !this.facade.unsaved();
  }

  /** The same protection for closing the tab, where the router guard never runs. */
  @HostListener('window:beforeunload', ['$event'])
  protected onBeforeUnload(event: BeforeUnloadEvent): void {
    if (this.facade.unsaved()) {
      event.preventDefault();
    }
  }

  protected readonly panel = signal<'layers' | 'library'>('layers');
  /** Both side panes collapse so the canvas can take the whole viewport — the full-website view. */
  protected readonly leftOpen = signal(true);
  protected readonly rightOpen = signal(true);
  protected readonly publishing = signal(false);
  protected readonly published = signal(false);

  protected readonly devices: readonly BuilderDevice[] = ['desktop', 'tablet', 'mobile'];

  protected readonly saveLabelKey = computed(() => {
    switch (this.facade.saveState()) {
      case 'pending':
      case 'saving':
        return 'builder.top.saving';
      case 'saved':
        return 'builder.top.saved';
      case 'conflict':
        return 'builder.top.conflict';
      case 'error':
        return 'builder.top.error';
      default:
        return this.facade.dirty() ? 'builder.top.unpublished' : 'builder.top.live';
    }
  });

  protected readonly revisionsOpen = signal(false);

  constructor() {
    // The selected section rides in the URL fragment (#sec-…): a refresh or a shared link lands
    // back on the same block, and replaceState keeps the browser's back button out of it.
    effect(() => {
      const id = this.facade.selectedId();
      const base = `${window.location.pathname}${window.location.search}`;
      window.history.replaceState(null, '', id ? `${base}#${id}` : base);
    });
  }

  ngOnInit(): void {
    this.facade.openLibrary = () => this.panel.set('library');
    this.facade.load(window.location.hash.slice(1) || null);
  }

  protected toggleRevisions(): void {
    this.revisionsOpen.update((open) => !open);
    if (this.revisionsOpen()) {
      this.facade.loadRevisions();
    }
  }

  /** Restoring overwrites the current draft — worth one explicit yes. */
  protected restoreRevision(version: number): void {
    if (window.confirm(this.transloco.translate('builder.revisions.confirm'))) {
      this.facade.restoreRevision(version);
    }
  }

  /** Leaving costs nothing — the draft autosaves — as long as the pending debounce is flushed first. */
  ngOnDestroy(): void {
    void this.facade.saveNow();
  }

  @HostListener('window:keydown', ['$event'])
  protected onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) {
      return; // text fields keep their own undo and Delete
    }
    if (event.key === 'Escape' && this.revisionsOpen()) {
      this.revisionsOpen.set(false);
      return;
    }
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'z') {
      event.preventDefault();
      if (event.shiftKey) {
        this.facade.redo();
      } else {
        this.facade.undo();
      }
      return;
    }
    if (event.key === 'Delete' || event.key === 'Backspace') {
      if (target?.closest('button')) {
        return; // focus is on a control (a toggle, a select) — Backspace there must not eat a section
      }
      const id = this.facade.selectedId();
      if (id) {
        event.preventDefault();
        this.facade.remove(id);
      }
    }
  }

  protected deviceIcon(device: BuilderDevice): IconName {
    return device === 'desktop' ? 'desktop' : device === 'tablet' ? 'box' : 'phone';
  }

  protected openPreviewTab(): void {
    const origin = this.facade.storefrontOrigin();
    const token = this.facade.previewToken();
    if (origin && token) {
      window.open(`${origin}/${this.facade.lang()}/?preview=${encodeURIComponent(token)}`, '_blank', 'noopener');
    }
  }

  protected async publish(): Promise<void> {
    this.publishing.set(true);
    const ok = await this.facade.publish();
    this.publishing.set(false);
    this.published.set(ok);
    if (ok) {
      if (this.revisionsOpen()) {
        this.facade.loadRevisions();
      }
      setTimeout(() => this.published.set(false), 3000);
    }
  }
}
