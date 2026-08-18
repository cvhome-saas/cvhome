import {Component, input, output, signal} from '@angular/core';

/**
 * The dashed drop zone the console uses for images — a logo tile, a banner, a set of
 * slides.
 *
 * Wraps a visually hidden `<input type="file">` inside its label, so clicking, tabbing and
 * dragging all reach the same control. The mockup's version was a `<div>`, which only
 * pointers could use.
 *
 * Whatever the zone should preview — a logo tile, a placeholder glyph — is projected:
 *
 * ```html
 * <app-file-drop label="Drop image or browse" hint="PNG or SVG · max 2 MB" accept="image/*">
 *   <span class="logo-tile">A</span>
 * </app-file-drop>
 * ```
 */
@Component({
  selector: 'app-file-drop',
  template: `
    <!-- The input is a descendant, so the label names it without an id. -->
    <label class="drop" [class.over]="isOver()">
      <input
        class="sr-only"
        type="file"
        [attr.accept]="accept()"
        [multiple]="multiple()"
        (change)="onPicked($event)"
      />

      <ng-content />

      <span class="drop-label">{{ label() }}</span>
      @if (hint()) {
        <span class="drop-hint">{{ hint() }}</span>
      }
    </label>
  `,
  styleUrl: './file-drop.css',
  host: {
    '(dragover)': 'onDragOver($event)',
    '(dragleave)': 'isOver.set(false)',
    '(drop)': 'onDrop($event)',
  },
})
export class FileDrop {
  readonly label = input.required<string>();
  /** The constraints, said under the label: size, dimensions, format. */
  readonly hint = input<string | null>(null);
  readonly accept = input<string | null>('image/*');
  readonly multiple = input(false);

  readonly filesSelected = output<readonly File[]>();

  protected readonly isOver = signal(false);

  protected onPicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.emit(input.files);
    // Cleared so picking the same file twice still fires a change.
    input.value = '';
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isOver.set(true);
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isOver.set(false);
    this.emit(event.dataTransfer?.files ?? null);
  }

  private emit(files: FileList | null): void {
    const picked = Array.from(files ?? []);
    if (picked.length > 0) {
      this.filesSelected.emit(this.multiple() ? picked : picked.slice(0, 1));
    }
  }
}
