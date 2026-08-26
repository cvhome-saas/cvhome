import {Component, inject, input, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {ImageBroken} from '@shared/directives/image-broken';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {Icon} from '@shared/ui/icon/icon';
import {MediaPickerDialog} from '../../../content/components/media-picker/media-picker-dialog';
import {ImagePreview} from '@shared/ui/image-preview/image-preview';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import type {MediaAsset} from '@models/content';
import type {ProductImageItem} from '@models/products';
import {ProductFormFacade} from '../../facades/product-form.facade';

/**
 * Step 2 — the product's pictures, picked from the store's media library.
 *
 * **Needs a saved product.** Images attach to `…/product/{id}/images`, so there is nothing to
 * attach them to until the product exists. The step is disabled in the rail rather than discovered
 * by clicking a dead well, and Save draft is the way through.
 *
 * The bytes are not this step's business any more: the picker uploads into the library, where an
 * image is deduplicated, measured, given alt text and reusable across products, and this step
 * stores the asset id. That is also what made the default image changeable — it used to be decided
 * at upload and never again.
 */
@Component({
  selector: 'app-media-step',
  imports: [ConfirmDialog, Icon, ImageBroken, ImagePreview, MediaPickerDialog, NoticeBar, Panel, TranslocoDirective],
  templateUrl: './media-step.html',
  styleUrls: ['../editor-card.css', './media-step.css'],
})
export class MediaStep {
  readonly saved = input.required<boolean>();

  protected readonly facade = inject(ProductFormFacade);

  /** Images whose URL this browser could not reach — the pod's paths are not always public. */
  protected readonly broken = signal<ReadonlySet<number>>(new Set());

  /** The image being looked at full size. One at a time, so one dialog serves the gallery. */
  protected readonly viewing = signal<ProductImageItem | null>(null);

  protected markBroken(id: number): void {
    this.broken.update((current) => new Set(current).add(id));
  }

  /** The library browser, opened from the last cell of the grid. */
  protected readonly picking = signal(false);

  /**
   * Attaches the picked asset, carrying its name across.
   *
   * The title first, then the file it was uploaded as. Sending `null` when an asset has no title —
   * which most do not — left the storefront rendering `alt=""` and left this grid captioning the
   * tile with the gallery row's database id, because that was the only thing the read had to fall
   * back on. A filename is a worse alt text than a written one and a much better one than nothing.
   */
  protected onPicked(asset: MediaAsset): void {
    this.picking.set(false);
    const name = asset.title?.trim() || asset.originalFilename || null;
    this.facade.attachImages([{mediaAssetId: asset.id, altText: name}]);
  }

  protected onViewerToggled(open: boolean): void {
    if (!open) {
      this.viewing.set(null);
    }
  }
  /** Which image the operator has asked to remove, while they confirm it. */
  protected readonly pendingRemove = signal<number | null>(null);

  protected confirmRemove(): void {
    const id = this.pendingRemove();
    this.pendingRemove.set(null);
    if (id !== null) {
      this.facade.removeImage(id);
    }
  }

}
