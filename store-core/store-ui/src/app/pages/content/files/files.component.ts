import {Component} from '@angular/core';
import {CrudService} from '../../shared/services/crud.service';
import {NbDialogService} from '@nebular/theme';
import {Lightbox} from 'ngx-lightbox';
import {IAlbum} from "ngx-lightbox/lightbox-event.service";
import {ShowcaseDialogComponent} from "../../shared/components/showcase-dialog/showcase-dialog.component";
import {NgxFileDropEntry} from "ngx-file-drop";
import {FileSystemFileEntry} from "ngx-file-drop/lib/dom.types";

@Component({
  selector: 'files-content',
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.scss'],
})
export class FilesComponent {
  store: string = '';
  loadingList = false;
  files: FileImage[] = [];
  images: FileImage[] = [];

  constructor(
    private crudService: CrudService,
    private dialogService: NbDialogService,
    private _lightbox: Lightbox) {
  }

  onSelectStore(e) {
    this.store = e.id
    this.getFiles();
  }

  getFiles() {
    this.loadingList = true;
    this.crudService.get('/store/api/v1/content/images?store=' + this.store)
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
          this.loadingList = false;
        },
        error: (e) => {
          this.loadingList = false;
        },
        complete: () => {
          this.images = [];
        }
      })

  }

  removeImage(e) {
    this.loadingList = true;
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    })
      .onClose.subscribe(res => {
      if (res) {
        this.loadingList = true;
        this.crudService.delete(`/store/api/v1/private/content/?store=${this.store}&contentType=IMAGE&name=${e}`)
          .subscribe({
            next: (data) => {
              this.loadingList = false;
              this.getFiles();
            },
            error: (data) => {
              this.loadingList = false;
            },
          });
      } else {
        this.loadingList = false;
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
      this.crudService.post(`/store/api/v1/private/files?store=${this.store}`, formData)
        .subscribe({
          next: (data) => {
            console.log(data);
            this.loadingList = false;
            this.getFiles();
          },
          error: (err) => {
            this.loadingList = false;
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
