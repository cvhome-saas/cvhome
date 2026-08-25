import {
  Component,
  ElementRef,
  computed,
  effect,
  input,
  output,
  viewChild,
} from '@angular/core';

/**
 * A modal video player for a single clip.
 *
 * Built on the native `<dialog>` element for the same reasons `ConfirmDialog` is: `showModal()`
 * brings the top layer, the focus trap, the backdrop, `Escape` and `aria-modal` with it. The
 * player itself is a plain `<video controls>` rather than an embedded third-party iframe — the
 * console then owns the chrome, the keyboard behaviour and the privacy story, and no external
 * player script runs on a page behind the operator's session.
 *
 * **It plays whatever it is given.** `src` is just a URL, so an MP4 on the platform's own storage,
 * a signed CDN link, or a local asset all work without this component knowing the difference. A
 * `poster` is optional; `captions` takes a WebVTT track when one exists.
 *
 * ```html
 * <app-video-dialog
 *   [open]="playing()"
 *   [title]="video.title"
 *   [src]="video.src"
 *   (closed)="playing.set(false)"
 * />
 * ```
 */
@Component({
  selector: 'app-video-dialog',
  template: `
    <dialog #dialog class="vd" (close)="closed.emit()" (cancel)="closed.emit()">
      <div class="vd-body">
        <header class="vd-head">
          <h2 class="vd-title" dir="auto">{{ title() }}</h2>
          <button
            class="vd-close"
            type="button"
            [attr.aria-label]="closeLabel()"
            (click)="close()"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 6l12 12M18 6L6 18" stroke-linecap="round" />
            </svg>
          </button>
        </header>

        @if (src(); as source) {
          <!--
            preload="metadata" rather than "auto": the dialog is built with the page, and
            downloading a whole video for a player nobody has opened is bandwidth spent on nothing.
          -->
          <video
            #player
            class="vd-player"
            controls
            playsinline
            preload="metadata"
            [poster]="poster() ?? undefined"
            [attr.aria-label]="title()"
          >
            <source [src]="source" />
            @if (captions(); as track) {
              <track kind="captions" [src]="track" [srclang]="captionsLang()" default />
            }
            {{ unsupportedLabel() }}
          </video>
        } @else {
          <!-- No source is a content gap, not a player fault, and says so rather than showing a black box. -->
          <p class="vd-missing" role="status">{{ unavailableLabel() }}</p>
        }

        @if (description(); as copy) {
          <p class="vd-copy" dir="auto">{{ copy }}</p>
        }
      </div>
    </dialog>
  `,
  styleUrl: './video-dialog.css',
})
export class VideoDialog {
  readonly open = input(false);
  readonly title = input.required<string>();
  /** The clip. Any URL a `<video>` can load; null renders the unavailable line instead. */
  readonly src = input<string | null>(null);
  readonly poster = input<string | null>(null);
  readonly description = input<string | null>(null);
  /** A WebVTT captions track, when the clip has one. */
  readonly captions = input<string | null>(null);
  readonly captionsLang = input('en');

  readonly closeLabel = input('Close');
  readonly unavailableLabel = input('This video is not available yet.');
  readonly unsupportedLabel = input('Your browser cannot play this video.');

  readonly closed = output<void>();

  /** Optional rather than `required` for the same reason as `PlanDialog`: an effect must be able to
   * read it before the view exists without throwing NG0951. */
  private readonly dialog = viewChild<ElementRef<HTMLDialogElement>>('dialog');
  private readonly player = viewChild<ElementRef<HTMLVideoElement>>('player');

  /** Re-reading `src` here is what makes a source swap reset the element rather than resume mid-clip. */
  protected readonly currentSource = computed(() => this.src());

  constructor() {
    effect(() => {
      const element = this.dialog()?.nativeElement;
      if (!element) {
        return;
      }
      if (this.open()) {
        if (!element.open) {
          element.showModal();
        }
        return;
      }
      if (element.open) {
        element.close();
      }
    });

    /*
     * Closing has to stop playback. A `<dialog>` that is closed is merely hidden — the element
     * keeps playing, so the operator gets audio from a dialog they cannot see. Rewinding as well
     * means reopening starts the clip rather than resuming a half-watched one.
     */
    effect(() => {
      if (this.open()) {
        return;
      }
      const video = this.player()?.nativeElement;
      if (video) {
        video.pause();
        video.currentTime = 0;
      }
    });
  }

  protected close(): void {
    this.dialog()?.nativeElement.close();
  }
}
