import {Component, computed, input, output, signal} from '@angular/core';

import {Icon} from '@shared/ui/icon/icon';

/**
 * A multi-file drop target with a Browse button — the media library's uploader. Validates nothing
 * itself beyond the `accept` list and the byte cap, and reports what it refused; the server is the
 * authority on types, sizes and quota.
 *
 * `busy` turns the zone into a progress surface; `percent` drives the bar.
 */
@Component({
  selector: 'app-file-drop-zone',
  imports: [Icon],
  template: `
    <div
      class="zone"
      [class.dragging]="dragging()"
      [class.busy]="busy()"
      (dragover)="onDragOver($event)"
      (dragleave)="dragging.set(false)"
      (drop)="onDrop($event)"
    >
      <span class="zone-icon" aria-hidden="true"><app-icon name="upload" [size]="20" /></span>
      <span class="zone-copy">
        <strong>{{ title() }}</strong>
        <small>{{ hint() }}</small>
        @if (rejected(); as message) {
          <small class="rejected" role="alert">{{ message }}</small>
        }
      </span>
      <span class="zone-spacer"></span>
      @if (busy()) {
        <span class="progress" role="progressbar" [attr.aria-valuenow]="percent()" aria-valuemin="0" aria-valuemax="100">
          <span class="bar" [style.inline-size.%]="percent()"></span>
        </span>
      } @else {
        <button class="primary-action" type="button" (click)="fileInput.click()">{{ browseLabel() }}</button>
      }
      <input #fileInput class="sr-only" type="file" multiple [accept]="accept()" [attr.aria-label]="browseLabel()" (change)="onPicked($event)" />
    </div>
  `,
  styleUrl: './file-drop-zone.css',
})
export class FileDropZone {
  readonly title = input.required<string>();
  readonly hint = input.required<string>();
  readonly browseLabel = input.required<string>();
  /** Comma-separated accept list, as the file input takes it. */
  readonly accept = input('image/*,video/mp4,application/pdf,application/zip');
  readonly maxBytes = input(50 * 1024 * 1024);
  /** Text for a file refused by size; receives `{name}`. Produced by the consumer so it is translated. */
  readonly tooLargeLabel = input<(name: string) => string>((name) => name);
  readonly busy = input(false);
  readonly percent = input(0);

  readonly files = output<readonly File[]>();

  protected readonly dragging = signal(false);
  protected readonly rejected = signal<string | null>(null);

  protected readonly acceptList = computed(() => this.accept().split(',').map((a) => a.trim()).filter(Boolean));

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (!this.busy()) {
      this.dragging.set(true);
    }
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
    if (this.busy()) {
      return;
    }
    this.take(Array.from(event.dataTransfer?.files ?? []));
  }

  protected onPicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.take(Array.from(input.files ?? []));
    input.value = '';
  }

  private take(files: readonly File[]): void {
    const accepted: File[] = [];
    let refused: string | null = null;
    for (const file of files) {
      if (file.size > this.maxBytes()) {
        refused = this.tooLargeLabel()(file.name);
        continue;
      }
      accepted.push(file);
    }
    this.rejected.set(refused);
    if (accepted.length) {
      this.files.emit(accepted);
    }
  }
}
