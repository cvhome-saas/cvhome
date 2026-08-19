import {Directive, ElementRef, afterNextRender, inject, output} from '@angular/core';

/**
 * Reports an `<img>` that will not load, including one that had already failed before Angular
 * attached a listener.
 *
 * The `(error)` binding alone is not enough, and the way it fails is quiet: the browser starts the
 * request the moment `src` is set, and a URL that resolves instantly — from cache, or refused by a
 * host that is simply not reachable from this machine — can dispatch `error` before the listener
 * exists. The image then sits there as a broken glyph with the component still believing it loaded.
 * Checking `complete && naturalWidth === 0` after the first render catches exactly that case, and
 * the listener catches everything slower.
 *
 * Written for the store's marketing images, whose URLs are pod-internal paths this browser often
 * cannot reach at all — see lessons.md, "Orders — the store's logo URL is not reachable from the
 * browser". Not `NgOptimizedImage`: that wants width and height, which nothing records for these.
 */
@Directive({
  selector: 'img[appImageBroken]',
  host: {'(error)': 'broken.emit()'},
})
export class ImageBroken {
  private readonly element = inject<ElementRef<HTMLImageElement>>(ElementRef);

  readonly broken = output<void>();

  constructor() {
    afterNextRender(() => {
      const image = this.element.nativeElement;
      if (image.complete && image.naturalWidth === 0) {
        this.broken.emit();
      }
    });
  }
}
