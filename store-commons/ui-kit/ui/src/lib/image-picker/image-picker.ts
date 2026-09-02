import {Component, computed, effect, inject, input, output, signal, untracked} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {Icon} from '../icon/icon';
import {ImagePreview} from '../image-preview/image-preview';

/** What an image has to be before it is worth sending. */
export interface ImageRules {
  /** MIME types, exactly as the file input's `accept` wants them. */
  readonly accept: string;
  readonly maxBytes: number;
  readonly minWidth: number;
  readonly minHeight: number;
  /** Width ÷ height the frame is drawn at, and the shape an upload is measured against. */
  readonly aspect: number;
  /** How far from `aspect` an upload may sit before it is refused. `0.35` is ±35%. */
  readonly aspectTolerance: number;
}

/**
 * A framed image well: the asset itself is the control.
 *
 * Replaces the pattern this page used to have — a dashed box with a small preview floating inside
 * it above the drop label — where the picture read as loose cargo rather than as the thing the box
 * was for, and where a square upload and a wide one looked identical because nothing held a shape.
 * Here the frame is drawn at the asset's own aspect ratio and the image fills it, so what the
 * operator sees is what the storefront will render. The dashed treatment survives as the *empty*
 * state, which is the only state that is an invitation.
 *
 * It also refuses what the server would rather not have. The file input's `accept` is advisory —
 * every browser lets a determined operator pick anything — so type, weight and pixel dimensions are
 * all checked here, before the multipart POST, and a refusal names the actual number rather than
 * repeating the rule.
 */
@Component({
  selector: 'app-image-picker',
  imports: [Icon, ImagePreview],
  template: `
    <!--
      The frame holds two controls that overlap, which is why the eye is a sibling of the label
      rather than a child of it: any button inside a label activates that label's input, so an eye
      nested in the well would have opened the file picker instead of the preview.
    -->
    <div class="frame">
    <label
      class="well"
      [class.filled]="hasImage()"
      [class.over]="isOver()"
      [class.busy]="busy()"
      [style.--frame-aspect]="rules().aspect"
    >
      <input
        class="sr-only"
        type="file"
        [attr.accept]="rules().accept"
        [attr.aria-label]="pickLabel()"
        [disabled]="busy()"
        (change)="onPicked($event)"
      />

      @if (hasImage()) {
        <img class="asset" [src]="url()" [alt]="alt()" (error)="broken.set(true)" />

        <!-- Named on hover and focus, because a picture on its own does not say it is a button. -->
        <span class="veil" aria-hidden="true">
          <app-icon name="upload" />
          <span>{{ t('shared.imagePicker.replace') }}</span>
        </span>
      } @else {
        <span class="empty">
          <app-icon [name]="icon()" />
          <span class="empty-label">{{ label() }}</span>
          <span class="empty-hint">{{ constraints() }}</span>
        </span>
      }

      @if (busy()) {
        <span class="veil busy-veil" role="status">
          <span class="spinner" aria-hidden="true"></span>
          <span>{{ t('shared.imagePicker.uploading') }}</span>
        </span>
      } @else if (justSaved()) {
        <!--
          The upload ends where it started. A toast at the corner of the page answers "did anything
          happen" somewhere other than where the operator is looking, so the well says it itself and
          then gets out of the way.
        -->
        <span class="veil saved-veil" role="status">
          <app-icon name="checkCircle" />
          <span>{{ t('shared.imagePicker.saved') }}</span>
        </span>
      }
    </label>

      @if (hasImage()) {
        <!--
          Kept in the DOM rather than revealed on hover alone: a control that only exists for a
          pointer is a control a keyboard cannot reach. It fades in with the veil and is fully
          opaque whenever it holds focus.
        -->
        <button
          class="peek"
          type="button"
          [attr.aria-label]="peekLabel()"
          [title]="peekLabel()"
          (click)="openPreview()"
        >
          <app-icon name="eye" />
        </button>
      }
    </div>

    <!--
      One line under the frame at most, and only when it adds something. An empty well already
      states the rules inside itself, so repeating them here was the same sentence twice.
    -->
    @if (error(); as message) {
      <p class="foot error" role="alert">
        <app-icon name="alertCircle" />
        <span>{{ message }}</span>
      </p>
    } @else if (hasImage() && fileName()) {
      <p class="foot stored">{{ t('shared.imagePicker.stored', {name: fileName()}) }}</p>
    }

    <app-image-preview
      [(open)]="previewOpen"
      [url]="url()"
      [alt]="alt()"
      [heading]="assetName() || label()"
      [caption]="fileName()"
    />
  `,
  styleUrl: './image-picker.css',
  host: {
    '(dragover)': 'onDragOver($event)',
    '(dragleave)': 'isOver.set(false)',
    '(drop)': 'onDrop($event)',
  },
})
export class ImagePicker {
  private readonly transloco = inject(TranslocoService);

  /** The empty well's prompt — an invitation ("Drop an image, or browse"), not a name. */
  readonly label = input.required<string>();
  /**
   * What this asset is called: "Logo", "Banner", "Slide".
   *
   * Separate from `label` because the two are different sentences and only one of them belongs in
   * a control's accessible name. Borrowing the prompt produced "View Drop an image, or browse full
   * size", which is what a screen reader would have read out.
   */
  readonly assetName = input('');
  readonly rules = input.required<ImageRules>();
  /** What is stored, or `null` for an empty well. */
  readonly url = input<string | null>(null);
  readonly fileName = input<string | null>(null);
  readonly alt = input('');
  readonly icon = input<'images' | 'palette'>('images');
  readonly busy = input(false);

  /** A file that passed every rule. Anything else never leaves the component. */
  readonly picked = output<File>();

  protected readonly isOver = signal(false);
  protected readonly broken = signal(false);
  protected readonly error = signal<string | null>(null);

  /** True for a moment after an upload finishes, so the well confirms rather than just stopping. */
  protected readonly justSaved = signal(false);

  /** Whether the enlarged view is open. The dialog itself is `shared/ui/image-preview`. */
  protected readonly previewOpen = signal(false);

  private readonly wasBusy = signal(false);

  constructor() {
    /*
     * Watches the busy flag rather than the upload, because the component does not own the request
     * — the facade does, and it owns the failure too. A busy spell that ends is the only signal
     * available here, which is exactly the moment worth marking.
     */
    effect(() => {
      if (this.busy()) {
        this.wasBusy.set(true);
        this.justSaved.set(false);
        return;
      }
      if (untracked(() => this.wasBusy())) {
        this.wasBusy.set(false);
        this.justSaved.set(true);
        setTimeout(() => this.justSaved.set(false), SAVED_MS);
      }
    });
  }

  /**
   * Whether there is a picture to show.
   *
   * A stored image whose URL this browser cannot reach counts as none — the pod's asset paths are
   * often unreachable from the operator's machine (see lessons.md, "Orders — the store's logo URL
   * is not reachable from the browser"), and an empty well is a better answer than a broken glyph.
   */
  protected readonly hasImage = computed(() => Boolean(this.url()) && !this.broken());

  /** The rules as one line of prose, which is both the empty-state hint and the resting caption. */
  protected readonly constraints = computed(() => {
    const rules = this.rules();
    return this.transloco.translate('shared.imagePicker.constraints', {
      width: rules.minWidth,
      height: rules.minHeight,
      size: megabytes(rules.maxBytes),
      formats: formatNames(rules.accept),
    });
  });

  protected t(key: string, params?: Record<string, unknown>): string {
    return this.transloco.translate(key, params);
  }

  /** Falls back to the prompt when no name was given, so the control is never unlabelled. */
  private naming(): string {
    return this.assetName() || this.label();
  }

  protected pickLabel(): string {
    return this.transloco.translate('shared.imagePicker.choose', {field: this.naming()});
  }

  protected peekLabel(): string {
    return this.transloco.translate('shared.imagePicker.peek', {field: this.naming()});
  }

  protected openPreview(): void {
    this.previewOpen.set(true);
  }





  protected onPicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    void this.accept(input.files?.[0]);
    // Cleared so choosing the same file twice still fires — a retry after a refusal is exactly that.
    input.value = '';
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (!this.busy()) {
      this.isOver.set(true);
    }
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isOver.set(false);
    if (!this.busy()) {
      void this.accept(event.dataTransfer?.files?.[0]);
    }
  }

  /**
   * Checks a file against the rules and emits it, or explains why not.
   *
   * Ordered cheapest first: type and weight are properties of the file, while dimensions require
   * decoding it. A refusal quotes what the file actually is, because "must be at least 1200px wide"
   * under a picture the operator believes is fine is an argument, not an explanation.
   */
  private async accept(file: File | undefined): Promise<void> {
    if (!file) {
      return;
    }
    this.error.set(null);
    const rules = this.rules();

    if (!matchesAccept(file, rules.accept)) {
      this.fail('shared.imagePicker.wrongType', {formats: formatNames(rules.accept)});
      return;
    }
    if (file.size > rules.maxBytes) {
      this.fail('shared.imagePicker.tooHeavy', {
        actual: megabytes(file.size),
        limit: megabytes(rules.maxBytes),
      });
      return;
    }

    const size = await measure(file);
    if (size === 'unreadable') {
      this.fail('shared.imagePicker.unreadable');
      return;
    }
    /*
     * A vector has no intrinsic pixel size and no wrong shape — it is whatever the frame asks for —
     * so the two measurements below simply do not apply to it.
     */
    if (size === 'vector') {
      this.broken.set(false);
      this.picked.emit(file);
      return;
    }
    if (size.width < rules.minWidth || size.height < rules.minHeight) {
      this.fail('shared.imagePicker.tooSmall', {
        actual: `${size.width} × ${size.height}`,
        width: rules.minWidth,
        height: rules.minHeight,
      });
      return;
    }

    const ratio = size.width / size.height;
    if (Math.abs(ratio - rules.aspect) > rules.aspect * rules.aspectTolerance) {
      this.fail('shared.imagePicker.wrongShape', {
        actual: `${size.width} × ${size.height}`,
        wanted: `${rules.minWidth} × ${rules.minHeight}`,
      });
      return;
    }

    this.broken.set(false);
    this.picked.emit(file);
  }

  private fail(key: string, params?: Record<string, unknown>): void {
    this.error.set(this.transloco.translate(key, params));
  }
}

/** How long the well holds its confirmation before returning to the asset. */
const SAVED_MS = 1600;

/** `image/png,image/svg+xml` → does this file qualify. Wildcards included, because `accept` allows them. */
function matchesAccept(file: File, accept: string): boolean {
  return accept
    .split(',')
    .map((entry) => entry.trim())
    .some((entry) =>
      entry.endsWith('/*') ? file.type.startsWith(entry.slice(0, -1)) : file.type === entry,
    );
}

/** `image/svg+xml` → `SVG`. What an operator calls the format, not what the browser calls it. */
function formatNames(accept: string): string {
  return accept
    .split(',')
    .map((entry) => entry.trim().split('/')[1]?.split('+')[0]?.toUpperCase())
    .filter(Boolean)
    .join(' · ');
}

function megabytes(bytes: number): string {
  const mb = bytes / 1024 / 1024;
  return `${mb >= 10 ? Math.round(mb) : mb.toFixed(1)} MB`;
}

/**
 * The pixel dimensions of an image file.
 *
 * `createImageBitmap` decodes off the main thread, which is what makes this check affordable at
 * all — without it a 5 MB banner would block the frame it is being validated in. A file the
 * browser cannot decode is `unreadable`, which is a different answer from "too small" and gets a
 * different message: it usually means the extension is lying about the contents.
 */
async function measure(file: File): Promise<{width: number; height: number} | 'vector' | 'unreadable'> {
  if (file.type === 'image/svg+xml') {
    return 'vector';
  }
  try {
    const bitmap = await createImageBitmap(file);
    const size = {width: bitmap.width, height: bitmap.height};
    bitmap.close();
    return size;
  } catch {
    return 'unreadable';
  }
}
