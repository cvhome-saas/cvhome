import {Component, DestroyRef, EventEmitter, Input, Output, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule} from '@nebular/theme';
import {ImageUploadingFacade} from './facades/image-uploading.facade';

const REMOVE_FROM_QUEUE_DELAY_MS = 2000;

/** Minimal structural shape shared by the various image DTOs this generic
 *  uploader is bound to (catalogue ReadableImage, store SliderImage, …). */
export interface UploadedImageItem {
  id?: number | string;
  path?: string;
  imageUrl?: string;
}

@Component({
  selector: 'ngx-image-uploading',
  standalone: true,
  imports: [TranslateModule, NbButtonModule],
  templateUrl: './image-uploading.component.html',
  styleUrls: ['./image-uploading.component.scss'],
  providers: [ImageUploadingFacade]
})
export class ImageUploadingComponent {
  @Input() images: UploadedImageItem[] = [];
  @Input() addImageUrl: string;
  @Input() deleteImageUrl: string;

  @Output() remove = new EventEmitter<string>();
  @Output() update = new EventEmitter<{id: string | number; position: number}>();
  @Output() uploadError = new EventEmitter<string>();
  @Output() success = new EventEmitter<string>();
  @Output() fileAdded = new EventEmitter<boolean>();

  protected readonly facade = inject(ImageUploadingFacade);
  private readonly destroyRef = inject(DestroyRef);

  itemTrackBy(index: number, item: UploadedImageItem) {
    return item.id;
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    const files = target.files;
    this.handleFiles(files);
    target.value = '';
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    const files = event.dataTransfer?.files;
    if (files) {
      this.handleFiles(files);
    }
  }

  removeImage(image: UploadedImageItem) {
    this.remove.emit(`${image.id}`);
  }

  private handleFiles(files: FileList) {
    if (files && files.length > 0) {
      for (const file of Array.from(files)) {
        this.uploadFile(file);
      }
    }
  }

  private uploadFile(file: File) {
    this.facade.uploadFile(file, this.addImageUrl)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (result.type === 'success') {
          this.success.emit(file.name);
          this.fileAdded.emit(true);
          setTimeout(() => this.facade.removeFromQueue(result.item), REMOVE_FROM_QUEUE_DELAY_MS);
        } else if (result.type === 'error') {
          this.uploadError.emit(result.message);
        }
      });
  }
}
