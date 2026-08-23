import {Component, ElementRef, computed, effect, inject, input, output, signal, viewChild} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoDirective} from '@jsverse/transloco';

import {MediaService} from '@api/content/media.service';
import type {MediaAsset, MediaKind} from '@models/content';
import {FileDropZone} from '@shared/ui/file-drop-zone/file-drop-zone';
import {Icon} from '@shared/ui/icon/icon';
import {SearchBox} from '@shared/ui/search-box/search-box';

/**
 * "Choose from the media library" — a modal grid of the store's assets of one kind, with a search
 * box and an inline uploader. Picking emits the asset; the caller stores its id and shows its URL.
 */
@Component({
  selector: 'app-media-picker-dialog',
  imports: [FileDropZone, Icon, SearchBox, TranslocoDirective],
  template: `
    <dialog #dialog class="picker" (close)="closed.emit()" (cancel)="closed.emit()" *transloco="let t">
      <div class="picker-head">
        <h2>{{ t('content.mediaPicker.title') }}</h2>
        <app-search-box width="12rem" [value]="search()" (valueChange)="search.set($event)" [label]="t('content.media.search')" [placeholder]="t('content.media.searchPlaceholder')" />
        <button class="icon-action" type="button" [attr.aria-label]="t('content.action.close')" (click)="close()"><app-icon name="x" [size]="14" /></button>
      </div>
      <app-file-drop-zone
        class="picker-upload"
        [title]="t('content.mediaPicker.uploadTitle')"
        [hint]="t('content.media.dropHint')"
        [browseLabel]="t('content.media.browse')"
        [accept]="accept()"
        [busy]="uploading()"
        [percent]="percent()"
        (files)="upload($event)"
      />
      <div class="picker-body">
        @if (assets().length) {
          <ul class="picker-grid">
            @for (asset of assets(); track asset.id) {
              <li>
                <button class="asset" type="button" [class.current]="asset.id === currentId()" (click)="pick(asset)" [attr.aria-label]="asset.filename">
                  @if (asset.kind === 'IMAGE' || asset.kind === 'VECTOR') {
                    <img [src]="asset.url" alt="" loading="lazy" />
                  } @else {
                    <app-icon name="file" [size]="28" />
                  }
                  <span dir="ltr">{{ asset.filename }}</span>
                </button>
              </li>
            }
          </ul>
        } @else if (!resource.isLoading()) {
          <p class="picker-empty">{{ t('content.mediaPicker.empty') }}</p>
        }
      </div>
    </dialog>
  `,
  styleUrls: ['./media-picker-dialog.css', '../../../../shared/styles/dialog-motion.css'],
})
export class MediaPickerDialog {
  private readonly api = inject(MediaService);

  readonly open = input(false);
  readonly kind = input<MediaKind>('IMAGE');
  readonly accept = input('image/*');
  readonly currentId = input<number | null>(null);

  readonly picked = output<MediaAsset>();
  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  protected readonly search = signal('');
  protected readonly uploading = signal(false);
  protected readonly percent = signal(0);
  private readonly stamp = signal(0);

  protected readonly resource = rxResource({
    params: () => {
      this.stamp();
      return this.open() ? {q: this.search(), kind: this.kind()} : undefined;
    },
    stream: ({params}) => this.api.list({folder: null, kind: params.kind, q: params.q, used: null, page: 0, count: 60}),
  });

  protected readonly assets = computed<readonly MediaAsset[]>(() =>
    this.resource.hasValue() ? this.resource.value().content : [],
  );

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        if (!element.open) {
          element.showModal();
        }
      } else if (element.open) {
        element.close();
      }
    });
  }

  protected pick(asset: MediaAsset): void {
    this.picked.emit(asset);
    this.close();
  }

  protected close(): void {
    this.dialog().nativeElement.close();
  }

  protected upload(files: readonly File[]): void {
    this.uploading.set(true);
    this.api.upload(files, null).subscribe({
      next: (event) => {
        if (event.kind === 'progress') {
          this.percent.set(event.percent);
        } else {
          this.uploading.set(false);
          this.stamp.update((v) => v + 1);
          if (event.assets.length === 1) {
            this.pick(event.assets[0]);
          }
        }
      },
      error: () => this.uploading.set(false),
    });
  }
}
