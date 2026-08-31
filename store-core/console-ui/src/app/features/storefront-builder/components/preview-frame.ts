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
 * The canvas: the real storefront rendering the draft, in an iframe at the preview URL. WYSIWYG by
 * construction — nothing is approximated in the console. Selection flows both ways over postMessage
 * (origins checked on both sides); the iframe reloads after every landed save, which is the honest
 * cost of previewing through the server renderer.
 */
@Component({
  selector: 'app-builder-preview-frame',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <div class="canvas" [class]="'device-' + facade.device()">
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
      background: var(--surface-canvas, #ececf0); padding: 16px;
    }
    iframe {
      border: 1px solid var(--border, #d4d4d8); border-radius: 10px; background: #fff;
      /* no width transition: animating an iframe's width relayouts the embedded page every frame */
      width: 100%; height: 100%; box-shadow: 0 8px 24px rgb(0 0 0 / 0.08);
    }
    .device-tablet iframe { width: 768px; }
    .device-mobile iframe { width: 390px; }
    .placeholder {
      margin: auto; font-size: 13px; color: var(--text-muted, #71717a); max-width: 40ch; text-align: center;
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
      const {type, sectionId} = (event.data ?? {}) as {type?: string; sectionId?: string};
      if (type === 'sectionClicked' && sectionId) {
        this.facade.selectedId.set(sectionId);
      }
      if (type === 'ready') {
        this.postSelection();
      }
    };
    window.addEventListener('message', onMessage);
    destroyRef.onDestroy(() => window.removeEventListener('message', onMessage));

    // selection changes flow into the canvas: outline + scroll to the block being edited
    effect(() => {
      const id = this.facade.selectedId();
      if (id) {
        this.post({type: 'select', sectionId: id});
        this.post({type: 'scrollTo', sectionId: id});
      }
    });
  }

  private postSelection(): void {
    const id = this.facade.selectedId();
    if (id) {
      this.post({type: 'select', sectionId: id});
    }
  }

  private post(message: Record<string, unknown>): void {
    const origin = this.facade.storefrontOrigin();
    const target = this.frame()?.nativeElement.contentWindow;
    if (origin && target) {
      target.postMessage(message, origin);
    }
  }
}
