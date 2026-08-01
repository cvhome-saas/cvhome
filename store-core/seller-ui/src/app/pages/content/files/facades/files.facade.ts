import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {NbDialogService} from '@nebular/theme';
import {Lightbox} from 'ngx-lightbox';
import {IAlbum} from 'ngx-lightbox/lightbox-event.service';
import {ShowcaseDialogComponent} from '../../../shared/components/showcase-dialog/showcase-dialog.component';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {ErrorService} from '../../../shared/services/error.service';
import {ContentService} from '../../services/content.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ContentFileItem} from '../../models/content.model';

export interface FileImage extends IAlbum {
  file?: File;
  path?: string;
  name?: string;
}

@Injectable()
export class FilesFacade {
  private readonly contentService = inject(ContentService);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly lightbox = inject(Lightbox);

  readonly store = signal<string>('');
  readonly loader = signal<boolean>(false);
  readonly files = signal<FileImage[]>([]);
  readonly images = signal<FileImage[]>([]);

  init(destroyRef: DestroyRef): void {
    this.selectedStoreService.current()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
        next: (it) => {
          this.store.set(it);
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.getFiles();
        }
      });
  }

  getFiles(): void {
    this.loader.set(true);
    this.contentService.images().subscribe({
      next: (data) => {
        const mapped = data.content.map((img: ContentFileItem) => {
          const src = img.path + img.name;
          return {
            path: img.path,
            name: img.name,
            src: src,
            thumb: src,
          };
        });
        this.images.set(mapped);
      },
      error: (err) => {
        this.images.set([]);
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {
        this.loader.set(false);
      }
    });
  }

  removeImage(e: string): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.loader.set(true);
        this.contentService.deleteImage(e).subscribe({
          next: () => {
            this.loader.set(false);
            this.getFiles();
          },
          error: (err) => {
            this.loader.set(false);
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
      } else {
        this.loader.set(false);
      }
    });
  }

  openImage(data: FileImage[], index: number): void {
    this.lightbox.open(data, index);
  }

  unSelectImage(i: number): void {
    const currentFiles = [...this.files()];
    currentFiles.splice(i, 1);
    this.files.set(currentFiles);
  }

  readURL(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: ProgressEvent<FileReader>) => {
      const data: string = e.target.result as string;
      const item: FileImage = {
        file,
        src: data,
        thumb: data,
      };
      this.files.set([...this.files(), item]);
    };
    reader.readAsDataURL(file);
  }

  dropped(files: NgxFileDropEntry[]): void {
    this.files.set([]);
    files.forEach(droppedFile => {
      if (droppedFile.fileEntry.isFile) {
        const fileEntry: FileSystemFileEntry = droppedFile.fileEntry as FileSystemFileEntry;
        fileEntry.file((file: File) => {
          this.readURL(file);
        });
      }
    });
  }

  upload(): void {
    const formData = new FormData();
    const currentFiles = this.files();
    currentFiles.forEach(item => {
      if (item.file) {
        formData.append('files', item.file);
      }
    });
    if (currentFiles.length > 0) {
      this.contentService.saveImage(formData).subscribe({
        next: () => {
          this.loader.set(false);
          this.getFiles();
        },
        error: (err) => {
          this.loader.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
    }
    this.files.set([]);
  }
}
