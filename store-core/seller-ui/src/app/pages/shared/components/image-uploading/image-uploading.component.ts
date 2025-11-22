import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {HttpEventType} from "@angular/common/http";
import {catchError, map} from "rxjs/operators";
import {of} from "rxjs";
import {CrudService} from "../../services/crud.service";

interface UploadItem {
  file: File;
  progress: number;
  error: boolean;
}

@Component({
  selector: "ngx-image-uploading",
  standalone: false,
  templateUrl: "./image-uploading.component.html",
  styleUrls: ["./image-uploading.component.scss"],
})
export class ImageUploadingComponent implements OnInit {

  @Input() images: any[] = [];
  @Input() addImageUrl: string;
  @Input() deleteImageUrl: string;

  @Output() remove = new EventEmitter<string>();
  @Output() update = new EventEmitter<any>();
  @Output() error = new EventEmitter<string>();
  @Output() success = new EventEmitter<string>();
  @Output() fileAdded = new EventEmitter<any>();

  uploadQueue: UploadItem[] = [];

  constructor(private crudService: CrudService) {
  }

  ngOnInit() {
  }

  public itemTrackBy(index, item) {
    return item.id;
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    this.handleFiles(files);
    event.target.value = ''; // Reset input
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

  handleFiles(files: FileList) {
    if (files && files.length > 0) {
      for (let i = 0; i < files.length; i++) {
        this.uploadFile(files[i]);
      }
    }
  }

  uploadFile(file: File) {
    const item: UploadItem = {
      file: file,
      progress: 0,
      error: false
    };
    this.uploadQueue.push(item);

    const formData = new FormData();
    formData.append('file', file);

    this.crudService.request('POST', this.addImageUrl, formData, {
      reportProgress: true,
    })

      .pipe(
        map(event => {
          if (event.type === HttpEventType.UploadProgress) {
            if (event.total) {
              item.progress = Math.round(100 * event.loaded / event.total);
            }
          } else if (event.type === HttpEventType.Response) {
            item.progress = 100;
            this.success.emit(file.name);
            this.fileAdded.emit(true);
            // Remove from queue after delay
            setTimeout(() => {
              this.uploadQueue = this.uploadQueue.filter(i => i !== item);
            }, 2000);
          }
        }),
        catchError(err => {
          console.error(err);
          item.error = true;
          this.error.emit(err.message || 'Upload failed');
          return of(null);
        })
      ).subscribe();
  }

  removeImage(image) {
    this.remove.emit(image.id);
  }
}
