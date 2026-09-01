/** Console-native; not a port from seller-core. */
import {
  ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, output, signal, viewChild,
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';
import {catchError, of} from 'rxjs';

import {MediaService} from '@api/content/media.service';
import type {MediaAsset} from '@models/content';
import {Icon} from '@shared/ui/icon/icon';
import {SearchBox} from '@shared/ui/search-box/search-box';

/**
 * A small pick-from-the-library dialog for the builder's media fields. The content hub has a richer
 * picker of its own, but features must not import each other — and the builder needs only "point at
 * an existing image": browsing folders and uploading stay the media tab's job, one link away in the
 * dialog's footer.
 */
@Component({
  selector: 'app-builder-media-select',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, RouterLink, SearchBox, TranslocoDirective],
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
              @if (loading()) {
                @for (cell of skeleton; track cell) {
                  <div class="asset ghost" aria-hidden="true"></div>
                }
              } @else {
                @for (asset of assets(); track asset.id) {
                  <button type="button" class="asset" [class.current]="asset.id === currentId()"
                          [attr.aria-pressed]="asset.id === currentId()"
                          [title]="asset.title || asset.originalFilename"
                          (click)="picked.emit(asset)">
                    <img [src]="asset.url" [alt]="asset.title || asset.originalFilename" loading="lazy" />
                    @if (asset.id === currentId()) {
                      <span class="badge"><app-icon name="check" [size]="12" /></span>
                    }
                    <span class="caption">{{ asset.title || asset.originalFilename }}</span>
                  </button>
                } @empty {
                  <p class="empty">{{ t(term() ? 'builder.media.noMatches' : 'builder.media.empty') }}</p>
                }
              }
            </div>
            <footer class="sheet-foot">
              <span>{{ t('builder.media.count', {count: assets().length}) }}</span>
              <a routerLink="/content/media" class="manage" (click)="closed.emit()">
                {{ t('builder.media.manage') }}
              </a>
            </footer>
      </dialog>
    </ng-container>
  `,
  styles: `
    .sheet {
      width: min(720px, calc(100vw - 48px)); height: min(560px, 80vh); flex-direction: column;
      /* the global reset zeroes every margin — without this the native dialog loses its centering
         and pins to the viewport corner */
      margin: auto;
      background: var(--background); border-radius: var(--radius-lg); border: 1px solid var(--border);
      overflow: hidden; padding: 0; box-shadow: var(--lift);
    }
    .sheet[open] { display: flex; }
    .sheet::backdrop { background: color-mix(in srgb, var(--foreground) 40%, transparent); }
    .sheet-head {
      display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-block-end: 1px solid var(--border);
    }
    .sheet-head strong { font-size: 14px; white-space: nowrap; }
    .sheet-head app-search-box { flex: 1; }
    .assets {
      flex: 1; min-height: 0; overflow-y: auto; display: grid; gap: 10px; padding: 14px;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      align-content: start;
    }
    .asset {
      position: relative; border: 1px solid var(--border); border-radius: var(--radius-md); overflow: hidden;
      padding: 0; background: var(--muted); cursor: pointer; aspect-ratio: 1; text-align: start;
    }
    .asset:hover { border-color: var(--primary); }
    .asset:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
    .asset.current { border-color: var(--primary); box-shadow: 0 0 0 2px var(--primary-soft, var(--primary)); }
    .asset img { width: 100%; height: 100%; object-fit: cover; display: block; }
    .badge {
      position: absolute; top: 6px; inset-inline-end: 6px; display: inline-flex; align-items: center;
      justify-content: center; width: 20px; height: 20px; border-radius: 50%;
      background: var(--primary); color: var(--primary-foreground);
    }
    .caption {
      position: absolute; inset-inline: 0; bottom: 0; padding: 8px 8px 6px; font-size: 11px; line-height: 1.2;
      color: #fff; background: linear-gradient(transparent, rgb(0 0 0 / 0.55));
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      opacity: 0; transition: opacity 0.15s;
    }
    .asset:hover .caption, .asset:focus-visible .caption, .asset.current .caption { opacity: 1; }
    .ghost { animation: media-pulse 1.2s ease-in-out infinite; cursor: default; }
    @keyframes media-pulse { 0%, 100% { opacity: 0.55; } 50% { opacity: 1; } }
    @media (prefers-reduced-motion: reduce) { .ghost { animation: none; } }
    .empty { grid-column: 1 / -1; font-size: 13px; color: var(--muted-foreground); margin: 8px; }
    .sheet-foot {
      display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 10px 14px;
      border-block-start: 1px solid var(--border); font-size: 12px; color: var(--muted-foreground);
    }
    .manage { color: var(--primary); font-weight: 500; text-decoration: none; }
    .manage:hover { text-decoration: underline; }
  `,
})
export class BuilderMediaSelect {
  private readonly media = inject(MediaService);

  readonly open = input(false);
  /** The asset the field currently holds, marked in the grid so "replace" starts from what is there. */
  readonly currentId = input<number | null>(null);
  readonly picked = output<MediaAsset>();
  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly skeleton = Array.from({length: 12}, (unused, index) => index);

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

  protected readonly loading = computed(() => this.open() && this.assetsResource.isLoading());

  protected readonly assets = computed(() =>
    this.assetsResource.hasValue() ? (this.assetsResource.value()?.content ?? []) : [],
  );
}
