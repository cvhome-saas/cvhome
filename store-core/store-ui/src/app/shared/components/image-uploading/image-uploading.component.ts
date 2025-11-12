import {Component, EventEmitter, Input, OnInit, Output,} from "@angular/core";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
// import {ImageUploadingAdapter} from "./image-uploading-adapter";
// import {FilePreviewModel, ValidationError,} from "ngx-awesome-uploader";

@Component({
  selector: "ngx-image-uploading",
  standalone: false,
  templateUrl: "./image-uploading.component.html",
  styleUrls: ["./image-uploading.component.scss"],
})
export class ImageUploadingComponent implements OnInit {
  // public adapter;

  @Input() images;
  @Input() addImageUrl;
  @Input() deleteImageUrl;

  @Output() remove = new EventEmitter<string>();
  @Output() update = new EventEmitter<any>();
  @Output() error = new EventEmitter<string>();
  @Output() success = new EventEmitter<string>();
  @Output() fileAdded = new EventEmitter<any>();


  addUrl;
  details = true;
  timer: any;
  uploadItemTemplate: any;

  constructor(
    private http: HttpClient,
  ) {
  }


  ngOnInit() {
    this.addUrl = this.addImageUrl;
    // this.adapter = new ImageUploadingAdapter(this.http, this.addUrl);
  }

  public itemTrackBy(item) {
    return item.id;
  }

  onUploadError(errorResponse: HttpErrorResponse) {
  }


  errorImage(code) {
    console.log("Error image " + code);
    this.error.emit(code);
  }

  removeImage(image) {
    this.remove.emit(image.id);
  }

  // public onValidationError(error: ValidationError): void {
  //   this.errorImage(error.error);
  // }
  //
  // public onUploadSuccess(e: FilePreviewModel): void {
  //   var me = this;
  //   this.timer = setTimeout(() => {
  //     me.success.emit(e.fileName);
  //     me.details = false;
  //   }, 2000);
  // }

  /* remove success */
  // public onRemoveSuccess(e: FilePreviewModel) {
  //   console.log("Remove");
  //   console.log(e);
  // }
  //
  // public onFileAdded(file: FilePreviewModel) {
  //   console.log("File added ", file);
  //   this.fileAdded.emit(true);
  //   clearTimeout(this.timer);
  // }
}
