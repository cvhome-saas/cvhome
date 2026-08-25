import {
  Component,
  ElementRef,
  effect,
  inject,
  input,
  model,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';

/** The exit animation's length, matching `.preview.closing` in the stylesheet. */
const EXIT_MS = 220;

/**
 * One stored image, shown at its own size.
 *
 * The console's wells are small on purpose — a square the width of a column for a logo, a 4:1 strip
 * for a banner, a thumbnail in a slide row — which is right for judging placement and useless for
 * judging the asset. This is where an operator goes to actually look at it.
 *
 * A native `<dialog>`, as `shared/ui/confirm-dialog` established: `showModal()` brings the top
 * layer, the focus trap, the backdrop and `Escape` with it, none of which a floating `<div>` gets
 * for free.
 *
 * It animates *out* as well as in, which a dialog does not do by default: `close()` flips `display`
 * to `none` immediately and a discrete property takes no animation with it. The CSS-only answer —
 * `transition-behavior: allow-discrete` with `@starting-style` — did not survive Angular's scoped
 * stylesheets here, so the exit is driven explicitly: a `closing` class plays the reverse keyframe
 * and the dialog is not actually closed until it finishes.
 *
 * ```html
 * <app-image-preview [(open)]="viewing" [url]="slide.url" [heading]="'Slide 3'" [caption]="slide.name" />
 * ```
 */
@Component({
  selector: 'app-image-preview',
  imports: [Icon],
  template: `
    <dialog
      #dialog
      class="preview"
      [class.closing]="closing()"
      (close)="onDialogClosed()"
      (animationend)="onAnimationEnd($event)"
    >
      @if (mounted()) {
        <header class="preview-bar">
          <span class="preview-title">{{ heading() }}</span>
          @if (naturalSize(); as size) {
            <span class="preview-size">{{ size }}</span>
          }
          @if (caption(); as text) {
            <span class="preview-name">{{ text }}</span>
          }
          <button
            class="preview-close"
            type="button"
            [attr.aria-label]="closeLabel()"
            (click)="close()"
          >
            <app-icon name="x" />
          </button>
        </header>

        <!-- A muted ground under the asset, so a transparent PNG reads as transparent. -->
        <div class="preview-stage">
          <img class="preview-image" [src]="url()" [alt]="alt()" (load)="onMeasured($event)" />
        </div>
      }
    </dialog>
  `,
  styleUrl: './image-preview.css',
  host: {
    '(click)': 'onDocumentClick($event)',
  },
})
export class ImagePreview {
  private readonly transloco = inject(TranslocoService);

  readonly open = model(false);
  readonly url = input<string | null>(null);
  /** What the asset is called — "Banner", "Slide 3". */
  readonly heading = input('');
  /** The stored filename, if there is one worth showing. */
  readonly caption = input<string | null>(null);
  readonly alt = input('');

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  /**
   * Whether the picture is in the DOM.
   *
   * Tracked apart from `open` because the dialog transitions *out*: dropping the content the moment
   * it is asked to close would fade an empty panel. The picture stays until the transition has
   * finished, and only then stops decoding.
   */
  protected readonly mounted = signal(false);
  /** True while the exit animation is playing and the dialog is deliberately still open. */
  protected readonly closing = signal(false);
  protected readonly naturalSize = signal<string | null>(null);

  constructor() {
    effect(() => {
      const element = this.dialog().nativeElement;
      if (this.open()) {
        untracked(() => {
          this.naturalSize.set(null);
          this.closing.set(false);
          this.mounted.set(true);
        });
        if (!element.open) {
          element.showModal();
        }
      } else if (element.open) {
        untracked(() => this.beginClosing());
      }
    });
  }

  protected closeLabel(): string {
    return this.transloco.translate('shared.imagePreview.close');
  }

  protected close(): void {
    this.open.set(false);
  }

  /**
   * Starts the exit and lets it finish before the dialog actually closes.
   *
   * The timer is the fallback, not the mechanism: `animationend` ends it as soon as the keyframe
   * does, and this only fires if there was no animation to wait for — a browser that skipped it, or
   * `prefers-reduced-motion`, which the global stylesheet cuts to a hundredth of a millisecond.
   */
  private beginClosing(): void {
    if (this.closing()) {
      return;
    }
    this.closing.set(true);
    setTimeout(() => this.finishClosing(), EXIT_MS);
  }

  private finishClosing(): void {
    if (!this.closing()) {
      return;
    }
    this.closing.set(false);
    const element = this.dialog().nativeElement;
    if (element.open) {
      element.close();
    }
    this.mounted.set(false);
  }

  protected onAnimationEnd(event: AnimationEvent): void {
    if (this.closing() && event.target === this.dialog().nativeElement) {
      this.finishClosing();
    }
  }

  /** `Escape` closes the dialog itself, so the model has to be told rather than the other way round. */
  protected onDialogClosed(): void {
    this.closing.set(false);
    this.mounted.set(false);
    this.open.set(false);
  }

  /**
   * Closes on a click that lands on the backdrop rather than on the picture.
   *
   * A modal dialog's backdrop is drawn by the element itself, so there is no node to bind to — the
   * test is whether the click's target *is* the dialog, which only happens when it missed
   * everything inside. Bound from the host rather than in the template on purpose: a template
   * `(click)` there trips the a11y rule that wants a keyboard equivalent, and the keyboard
   * equivalent is `Escape`, which the native dialog already handles.
   */
  protected onDocumentClick(event: MouseEvent): void {
    if (this.open() && event.target === this.dialog().nativeElement) {
      this.close();
    }
  }


  protected onMeasured(event: Event): void {
    const image = event.target as HTMLImageElement;
    this.naturalSize.set(`${image.naturalWidth} × ${image.naturalHeight}`);
  }
}
