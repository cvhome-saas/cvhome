/** Console-native; not a port from seller-core. */
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  computed,
  effect,
  inject,
  viewChild,
} from '@angular/core';
import {DomSanitizer, type SafeResourceUrl} from '@angular/platform-browser';
import {TranslocoDirective} from '@jsverse/transloco';

import {BuilderFacade} from '../facades/builder.facade';

/**
 * The canvas: the real storefront rendering the draft, in an iframe at the preview URL — WYSIWYG by
 * construction. The iframe is cross-origin, so the storefront's bridge draws every canvas affordance
 * and this component speaks protocol v2 with it (origins checked both ways, `v: 2` on every message):
 * selection/hover both directions, guides, locks, the floating toolbar's intents, add-here targeting,
 * in-canvas reorder, and — because native drags cannot cross into another origin's document — the
 * wrapper is the drop target for library drags, forwarding pointer Y so the bridge can place the
 * insertion line and answer with the boundary. The iframe reloads after every landed save, which is
 * the honest cost of previewing through the server renderer.
 */
@Component({
  selector: 'app-builder-preview-frame',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <div
        class="canvas"
        [class]="'device-' + facade.device()"
        [class.dragging]="!!facade.dragging()"
        (dragover)="onDragOver($event)"
        (drop)="onDrop($event)"
      >
        @if (url(); as src) {
          <iframe #frame [src]="src" [title]="t('builder.canvas.title')"></iframe>
        } @else {
          <div class="placeholder">{{ t('builder.canvas.unavailable') }}</div>
        }
      </div>
    </ng-container>
  `,
  styles: `
    :host { display: block; height: 100%; min-height: 0; }
    .canvas {
      height: 100%; display: flex; justify-content: center; overflow: auto;
      background: var(--surface-canvas, var(--muted)); padding: 16px;
    }
    iframe {
      border: 1px solid var(--border); border-radius: var(--radius-lg); background: var(--background);
      /* no width transition: animating an iframe's width relayouts the embedded page every frame */
      width: 100%; height: 100%;
    }
    /*
     * While a library tile is being dragged, the iframe must not hit-test: a native drag over an
     * iframe is delivered INTO the (cross-origin) document, where nothing accepts it — the wrapper's
     * dragover/drop never fire and the browser shows no-drop. Pointer-transparent for the drag's
     * duration, the wrapper receives the events and the bridge keeps drawing the insertion line.
     */
    .dragging iframe { pointer-events: none; }
    .device-tablet iframe { width: 768px; }
    .device-mobile iframe { width: 390px; }
    .placeholder {
      margin: auto; font-size: 13px; color: var(--muted-foreground); max-width: 40ch; text-align: center;
    }
  `,
})
export class BuilderPreviewFrame {
  protected readonly facade = inject(BuilderFacade);

  private readonly sanitizer = inject(DomSanitizer);
  private readonly frame = viewChild<ElementRef<HTMLIFrameElement>>('frame');

  /**
   * `savedRevision` is deliberately part of the URL: a landed save bumps it, the query changes, and the
   * iframe re-navigates to the fresh draft without any imperative reload bookkeeping.
   */
  protected readonly url = computed<SafeResourceUrl | null>(() => {
    const origin = this.facade.storefrontOrigin();
    const token = this.facade.previewToken();
    if (!origin || !token) {
      return null;
    }
    const lang = this.facade.lang();
    const raw = `${origin}/${lang}/?preview=${encodeURIComponent(token)}&_v=${this.facade.savedRevision()}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(raw);
  });

  constructor() {
    const destroyRef = inject(DestroyRef);
    const onMessage = (event: MessageEvent) => {
      if (event.origin !== this.facade.storefrontOrigin()) {
        return;
      }
      const data = (event.data ?? {}) as {v?: number; type?: string; sectionId?: string; beforeId?: string | null; action?: string};
      if (data.v !== 2) {
        return; // the handshake is versioned; anything unversioned is not the bridge speaking
      }
      switch (data.type) {
        case 'ready':
          // every iframe reload re-handshakes: replay the whole canvas state
          this.post({type: 'select', sectionId: this.facade.selectedId()});
          this.post({type: 'guides', on: this.facade.guides()});
          this.post({type: 'locks', ids: this.facade.lockedIds()});
          this.post({type: 'dragState', active: !!this.facade.dragging(), label: this.facade.dragging()?.label ?? ''});
          break;
        case 'sectionClicked':
          if (data.sectionId) {
            this.facade.selectedId.set(data.sectionId);
          }
          break;
        case 'sectionHovered':
          this.facade.hoveredId.set(data.sectionId ?? null);
          break;
        case 'toolbar':
          if (!data.sectionId) {
            break;
          }
          if (data.action === 'moveUp') {
            this.facade.moveById(data.sectionId, -1);
          } else if (data.action === 'moveDown') {
            this.facade.moveById(data.sectionId, 1);
          } else if (data.action === 'duplicate') {
            this.facade.duplicate(data.sectionId);
          } else if (data.action === 'remove') {
            this.facade.remove(data.sectionId);
          }
          break;
        case 'dropTarget':
          this.facade.dropBeforeId.set(data.beforeId ?? null);
          break;
        case 'reorder':
          if (data.sectionId) {
            this.facade.reorderBefore(data.sectionId, data.beforeId ?? null);
          }
          break;
        case 'addHere':
          this.facade.insertTarget.set(data.beforeId ?? null);
          this.facade.openLibrary?.();
          break;
      }
    };
    window.addEventListener('message', onMessage);
    destroyRef.onDestroy(() => window.removeEventListener('message', onMessage));

    // selection changes flow into the canvas: outline + scroll to the block being edited
    effect(() => {
      const id = this.facade.selectedId();
      this.post({type: 'select', sectionId: id});
      if (id) {
        this.post({type: 'scrollTo', sectionId: id});
      }
    });
    // hover, guides, locks and drag state mirror over as they change
    effect(() => this.post({type: 'hover', sectionId: this.facade.hoveredId()}));
    effect(() => this.post({type: 'guides', on: this.facade.guides()}));
    effect(() => this.post({type: 'locks', ids: this.facade.lockedIds()}));
    effect(() => {
      const drag = this.facade.dragging();
      this.post({type: 'dragState', active: !!drag, label: drag?.label ?? ''});
    });
  }

  // ------------------------------------------------------------------------------ library drag target

  protected onDragOver(event: DragEvent): void {
    if (!this.facade.dragging()) {
      return;
    }
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'copy';
    }
    const rect = this.frame()?.nativeElement.getBoundingClientRect();
    if (rect) {
      this.post({type: 'dragOver', y: event.clientY - rect.top});
    }
  }

  protected onDrop(event: DragEvent): void {
    if (!this.facade.dragging()) {
      return;
    }
    event.preventDefault();
    this.facade.dropDraggedAt(this.facade.dropBeforeId());
  }

  private post(message: Record<string, unknown>): void {
    const origin = this.facade.storefrontOrigin();
    const target = this.frame()?.nativeElement.contentWindow;
    if (origin && target) {
      target.postMessage({v: 2, ...message}, origin);
    }
  }
}
