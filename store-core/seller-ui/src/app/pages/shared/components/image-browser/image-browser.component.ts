import {Component, Input, OnInit} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';
import {CrudService} from "../../services/crud.service";
import {ErrorService} from "../../services/error.service";

@Component({
  selector: 'ngx-image-browser',
  standalone:false,
  templateUrl: './image-browser.component.html',
  styleUrls: ['./image-browser.component.scss']
})
export class ImageBrowserComponent implements OnInit {
  @Input()
  store: string;
  uploadedFiles: any[] = [];
  loadingList = false;

  constructor(
    private crudService: CrudService,
    protected ref: NbDialogRef<ImageBrowserComponent>,
    private errorService: ErrorService
  ) {
  }

  ngOnInit() {
    this.getImages();
  }

  getImages() {
    this.loadingList = true;
    this.crudService.get('/store-pod-gateway/merchant/api/v1/content/images')
      .subscribe({
        next: (data) => {
          this.uploadedFiles = data.content;
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  cancel() {
    this.ref.close();
  }

  openImage(value) {
    this.ref.close(value.path + value.name);
  }
}
