/** Console-native; not a port from seller-core. */
import {
  ChangeDetectionStrategy, Component, HostListener, type OnDestroy, type OnInit, computed, inject, signal,
} from '@angular/core';
import {DatePipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import type {ConfirmsLeave} from '@core/routing/confirm-leave.guard';
import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';

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
    BuilderInspector, BuilderLayerList, BuilderPreviewFrame, BuilderSectionLibrary, DatePipe, Icon, RouterLink,
    TranslocoDirective,
  ],
  templateUrl: './storefront-builder.html',
  styleUrl: './storefront-builder.css',
})
export class StorefrontBuilder implements ConfirmsLeave, OnInit, OnDestroy {
  protected readonly facade = inject(BuilderFacade);

  /** Unsaved edits (or a save in flight) ask before the route is left; see `confirmLeave`. */
  canLeave(): boolean {
    return !this.facade.dirty() && this.facade.saveState() !== 'saving';
  }

  protected readonly panel = signal<'layers' | 'library'>('layers');
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

  ngOnInit(): void {
    this.facade.openLibrary = () => this.panel.set('library');
    this.facade.load();
  }

  protected toggleRevisions(): void {
    this.revisionsOpen.update((open) => !open);
    if (this.revisionsOpen()) {
      this.facade.loadRevisions();
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
