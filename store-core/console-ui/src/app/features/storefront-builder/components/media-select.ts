/** Console-native; not a port from seller-core. */
import {
  ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, output, signal, viewChild,
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoDirective} from '@jsverse/transloco';
import {catchError, of} from 'rxjs';

import {MediaService} from '@api/content/media.service';
import type {MediaAsset} from '@models/content';
import {Icon} from '@shared/ui/icon/icon';
import {SearchBox} from '@shared/ui/search-box/search-box';

/**
 * A small pick-from-the-library dialog for the builder's media fields. The content hub has a richer
 * picker of its own, but features must not import each other — and the builder needs only "point at
 * an existing image": browsing folders and uploading stay the media tab's job.
 */
@Component({
  selector: 'app-builder-media-select',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, SearchBox, TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <dialog #dialog class="sheet" [attr.aria-label]="t('builder.media.title')"
              (close)="closed.emit()" (cancel)="closed.emit()">
            <header class="sheet-head">
              <strong>{{ t('builder.media.title') }}</strong>
              <app-search-box [label]="t('builder.media.title')" [value]="term()" (valueChange)="term.set($event)" />
              <button type="button" class="icon-action" [attr.aria-label]="t('builder.media.close')" (click)="closed.emit()">
                <app-icon name="x" [size]="15" />
              </button>
            </header>
            <div class="assets">
              @for (asset of assets(); track asset.id) {
                <button type="button" class="asset" [title]="asset.title || asset.originalFilename"
                        (click)="picked.emit(asset)">
                  <img [src]="asset.url" [alt]="asset.title || asset.originalFilename" loading="lazy" />
                </button>
              } @empty {
                <p class="empty">{{ t('builder.media.empty') }}</p>
              }
            </div>
      </dialog>
    </ng-container>
  `,
  styles: `
    .sheet {
      width: min(720px, calc(100vw - 48px)); max-height: 80vh; flex-direction: column;
      background: var(--background); border-radius: var(--radius-lg); border: 1px solid var(--border);
      overflow: hidden; padding: 0;
    }
    .sheet[open] { display: flex; }
    .sheet::backdrop { background: color-mix(in srgb, var(--foreground) 40%, transparent); }
    .sheet-head {
      display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-block-end: 1px solid var(--border);
    }
    .sheet-head strong { font-size: 14px; }
    .sheet-head app-search-box { flex: 1; }
    .assets {
      flex: 1; min-height: 0; overflow-y: auto; display: grid; gap: 8px; padding: 12px;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    }
    .asset {
      border: 1px solid var(--border); border-radius: var(--radius-md); overflow: hidden; padding: 0;
      background: var(--muted); cursor: pointer; aspect-ratio: 1;
    }
    .asset:hover { border-color: var(--primary); }
    .asset img { width: 100%; height: 100%; object-fit: cover; display: block; }
    .empty { grid-column: 1 / -1; font-size: 13px; color: var(--muted-foreground); margin: 8px; }
  `,
})
export class BuilderMediaSelect {
  private readonly media = inject(MediaService);

  readonly open = input(false);
  readonly picked = output<MediaAsset>();
  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  constructor() {
    effect(() => {
      const dialog = this.dialog().nativeElement;
      if (this.open() && !dialog.open) {
        dialog.showModal();
      } else if (!this.open() && dialog.open) {
        dialog.close();
      }
    });
  }

  protected readonly term = signal('');

  private readonly assetsResource = rxResource({
    params: () => ({open: this.open(), q: this.term()}),
    stream: ({params}) => {
      if (!params.open) {
        return of(null);
      }
      return this.media
        .list({kind: 'IMAGE', q: params.q, page: 0, count: 60, folder: null, used: null})
        .pipe(catchError(() => of(null)));
    },
  });

  protected readonly assets = computed(() =>
    this.assetsResource.hasValue() ? (this.assetsResource.value()?.content ?? []) : [],
  );
}
