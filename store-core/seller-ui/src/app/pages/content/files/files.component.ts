import {Component, OnInit} from '@angular/core';
import {NbDialogService} from '@nebular/theme';
import {Lightbox} from 'ngx-lightbox';
import {IAlbum} from "ngx-lightbox/lightbox-event.service";
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {FileSystemFileEntry, NgxFileDropEntry} from "ngx-file-drop";
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../services/content.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";

@Component({
  selector: 'files-content',
  standalone: false,
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.scss'],
})
export class FilesComponent implements OnInit {
  store: string = '';
  loader = false;
  files: FileImage[] = [];
  images: FileImage[] = [];

  constructor(
    private contentService: ContentService,
    private dialogService: NbDialogService,
    private errorService: ErrorService,
    private selectedStoreService: SelectedStoreService,
    private _lightbox: Lightbox) {
  }

  ngOnInit(): void {
    this.selectedStoreService.current().subscribe({
      next: (it) => {
        this.store = it;
      },
      error: err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {
        this.getFiles();
      }
    })
  }

  getFiles() {
    this.loader = true;
    this.contentService.images()
      .subscribe({
        next: (data: any) => {
          this.images = data.content.map(img => {
            const src = img.path + img.name;
            return {
              path: img.path,
              name: img.name,
              src: src,
              thumb: src,
            }
          });
        },
        error: (err) => {
          this.images = [];
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.loader = false;
        }
      })

  }

  removeImage(e) {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose
      .subscribe(res => {
        if (res) {
          this.loader = true;
          this.contentService.deleteImage( e)
            .subscribe({
              next: (data) => {
                this.loader = false;
                this.getFiles();
              },
              error: (err) => {
                this.loader = false;
                this.errorService.error('ERROR.SYSTEM_ERROR', err);
              },
            });
        } else {
          this.loader = false;
        }
      });
  }

  openImage(data: FileImage[], index: number): void {
    this._lightbox.open(data, index);
  }

  close(): void {
    this._lightbox.close();
  }

  unSelectImage(i: number) {
    this.files.splice(i, 1);
  }

  readURL(file: File) {
    const reader = new FileReader();
    const me = this;
    reader.onload = function (e) {
      const data: string = e.target.result as string;
      const item = {
        file,
        src: data,
        thumb: data,
      };
      me.files.push(item);
    };
    reader.readAsDataURL(file);
  }

  public dropped(files: NgxFileDropEntry[]) {
    this.files = [];
    files.forEach(droppedFile => {
      if (droppedFile.fileEntry.isFile) {
        const fileEntry: FileSystemFileEntry = droppedFile.fileEntry as FileSystemFileEntry;
        fileEntry.file((file: File) => {
          this.readURL(file)
        });
      }
    });

  }

  upload() {
    let formData = new FormData();
    this.files.forEach(item => {
      formData.append("files", item.file);
    });
    if (this.files.length > 0) {
      this.contentService.saveImage( formData)
        .subscribe({
          next: (data) => {
            console.log(data);
            this.loader = false;
            this.getFiles();
          },
          error: (err) => {
            this.loader = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
    }
    this.files = [];
  }
}


interface FileImage extends IAlbum {
  file?: File
  path?: string
  name?: string
}
