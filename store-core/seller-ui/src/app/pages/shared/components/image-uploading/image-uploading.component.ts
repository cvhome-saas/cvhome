import {Component, DestroyRef, EventEmitter, Input, Output, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ImageUploadingFacade} from './facades/image-uploading.facade';

const REMOVE_FROM_QUEUE_DELAY_MS = 2000;

@Component({
  selector: 'ngx-image-uploading',
  standalone: false,
  templateUrl: './image-uploading.component.html',
  styleUrls: ['./image-uploading.component.scss'],
  providers: [ImageUploadingFacade]
})
export class ImageUploadingComponent {
  @Input() images: any[] = [];
  @Input() addImageUrl: string;
  @Input() deleteImageUrl: string;

  @Output() remove = new EventEmitter<string>();
  @Output() update = new EventEmitter<any>();
  @Output() error = new EventEmitter<string>();
  @Output() success = new EventEmitter<string>();
  @Output() fileAdded = new EventEmitter<any>();

  protected readonly facade = inject(ImageUploadingFacade);
  private readonly destroyRef = inject(DestroyRef);

  itemTrackBy(index: number, item: any) {
    return item.id;
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    this.handleFiles(files);
    event.target.value = '';
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

  removeImage(image: any) {
    this.remove.emit(image.id);
  }

  private handleFiles(files: FileList) {
    if (files && files.length > 0) {
      for (let i = 0; i < files.length; i++) {
        this.uploadFile(files[i]);
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
          this.error.emit(result.message);
        }
      });
  }
}
