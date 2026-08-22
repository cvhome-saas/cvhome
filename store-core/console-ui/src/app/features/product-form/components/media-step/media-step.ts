import {Component, inject, input, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {ImageBroken} from '@shared/directives/image-broken';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {Icon} from '@shared/ui/icon/icon';
import {ImagePicker, type ImageRules} from '@shared/ui/image-picker/image-picker';
import {ImagePreview} from '@shared/ui/image-preview/image-preview';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {Panel} from '@shared/ui/panel/panel';
import type {ProductImageItem} from '@models/products';
import {ProductFormFacade} from '../../facades/product-form.facade';

/**
 * Step 2 — the product's pictures.
 *
 * **Needs a saved product.** Images post to `…/product/{id}/image`, so there is nothing to attach
 * them to until the product exists. The step is disabled in the rail rather than discovered by
 * clicking a dead well, and Save draft is the way through.
 *
 * **The default image cannot be changed.** The pod decides it at upload — the first image on a
 * product becomes the thumbnail — and no endpoint re-designates one: `PATCH …/image/{imageId}` sets
 * `sortOrder` and nothing else. So the storefront thumbnail is marked and not offered as a choice.
 * See lessons.md, "Catalogue — a product's default image cannot be changed after upload".
 *
 * Reordering *is* real, and the whole list is renumbered on every move because that `PATCH` does
 * not renumber the images it displaces.
 */
@Component({
  selector: 'app-media-step',
  imports: [ConfirmDialog, Icon, ImageBroken, ImagePicker, ImagePreview, NoticeBar, Panel, TranslocoDirective],
  templateUrl: './media-step.html',
  styleUrls: ['../editor-card.css', './media-step.css'],
})
export class MediaStep {
  readonly saved = input.required<boolean>();

  protected readonly facade = inject(ProductFormFacade);

  /**
   * What a product image has to be.
   *
   * Square, because that is the shape the storefront's category grid draws and a portrait
   * photograph dropped in becomes a band across its middle. 800px is the smallest that survives a
   * retina product page without visible softening.
   */
  protected readonly imageRules: ImageRules = {
    accept: 'image/jpeg,image/png,image/webp',
    maxBytes: 5 * 1024 * 1024,
    minWidth: 800,
    minHeight: 800,
    aspect: 1,
    aspectTolerance: 0.25,
  };

  /** Images whose URL this browser could not reach — the pod's paths are not always public. */
  protected readonly broken = signal<ReadonlySet<number>>(new Set());

  /** The image being looked at full size. One at a time, so one dialog serves the gallery. */
  protected readonly viewing = signal<ProductImageItem | null>(null);

  protected markBroken(id: number): void {
    this.broken.update((current) => new Set(current).add(id));
  }

  protected onPicked(file: File): void {
    this.facade.uploadImages([file]);
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
