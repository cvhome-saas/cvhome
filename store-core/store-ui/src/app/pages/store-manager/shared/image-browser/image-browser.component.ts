import {Component, Input, OnInit} from '@angular/core';
import {NbDialogRef} from '@nebular/theme';
import {CrudService} from "../../../shared/services/crud.service";

@Component({
  selector: 'ngx-image-browser',
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
    protected ref: NbDialogRef<ImageBrowserComponent>
  ) {
  }

  ngOnInit() {
    this.getImages(this.store);
  }

  getImages(store: string) {
    this.loadingList = true;
    this.crudService.get('/store/api/v1/content/images?store=' + store)
      .subscribe({
        next: (data) => {
          this.uploadedFiles = data.content;
          this.loadingList = false;
        },
        error: (err) => {
          this.loadingList = false;
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
