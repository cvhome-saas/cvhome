import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, Validators} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";
import {StoreService} from "../services/store.service";
import {NbToastrService} from "@nebular/theme";
import {Banner} from "../models/banner";

@Component({
  selector: 'ngx-store-branding-banner',
  template: `
    <form [formGroup]="imageUpload">
      <div (click)='imageInput.click()' (dragover)="allowDrop($event)" (drop)="drop($event)" class="wrapper">
        <div #imageDrop id="imageDrop">

          <img *ngIf="banner" [src]="banner?.path" style="height:250px"/>

        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input #imageInput (change)='imageChange($event)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        <button (click)="removeBanner()" *ngIf="showRemoveButton" class="btn btn-danger" type="submit">{{
            'COMMON.REMOVE' | translate
          }}
        </button>
        <button (click)="saveBanner()" *ngIf="bannerFile" [nbSpinner]="loadingButton" class="btn btn-primary" nbButton
                nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
          {{ (!loadingButton ? 'COMMON.SAVE' : '') | translate }}
          <!-- {{ 'COMMON.SAVE' | translate}} -->
        </button>
      </div>

    </form>
  `,
  styleUrls: ['./store-branding.component.scss']
})
export class StoreBrandingBannerComponent implements OnInit {
  @Input()
  store: any
  @ViewChild('imageDrop', {static: false}) imageDrop;
  acceptedImageTypes = {'image/png': true, 'image/jpeg': true, 'image/gif': true};
  imageUpload = this.formBuilder.group({
    imageInput: ['', Validators.required]
  });
  loadingButton = false;
  banner: Banner;

  bannerFile: any;
  showRemoveButton = false;

  constructor(private formBuilder: FormBuilder,
              private storeService: StoreService,
              private toastr: NbToastrService,
              private translate: TranslateService,
  ) {
  }

  ngOnInit(): void {

    this.banner = this.store.banner;
    if (this.banner) {
      this.showRemoveButton = true;
    }
  }


  // readfiles
  readfiles(files) {
    this.bannerFile = files[0];
    this.showRemoveButton = true;
    const reader = new FileReader();
    const image = new Image();
    reader.onload = (event) => {
      this.imageDrop.nativeElement.innerHTML = '';
      const fileReader = event.target as FileReader;
      image.src = fileReader.result as string;
      image.height = 200;
      image.style.display = 'block';
      image.style.margin = '0 auto';
      image.className = 'appendedImage';
      this.imageDrop.nativeElement.appendChild(image);
      if (this.imageUpload.controls.imageInput.value == null) {
        const input = this.imageUpload.controls.imageInput as any;
        input.files = files;
      }
    };
    reader.readAsDataURL(files[0]);

  }

  allowDrop(e) {
    e.preventDefault();
  }

  drop(e) {
    e.preventDefault();
    this.imageUpload.controls.imageInput.reset();
    this.imageDrop.innerHTML = '';
    this.checkfiles(e.dataTransfer.files);
  }

  // imageChange
  imageChange(event) {
    this.imageDrop.innerHTML = '';
    this.checkfiles(event.target.files);
  }

  saveBanner() {
    this.loadingButton = true;
    this.storeService.addStoreBanner(this.store.code, this.bannerFile)
      .subscribe({
        next: (data) => {
          this.toastr.success(this.translate.instant('STORE_BRANDING.BANNER_SAVED'));
          this.loadingButton = false;
        },
        error: (err) => {
          this.loadingButton = false;
        }
      })
  }

  checkfiles(files) {
    if (this.acceptedImageTypes[files[0].type] !== true) {
      this.imageDrop.nativeElement.innerHTML = this.translate.instant('STORE_BRANDING.NOT_AN_IMAGE');
      return;
    } else if (files.length > 1) {
      this.imageDrop.nativeElement.innerHTML = this.translate.instant('STORE_BRANDING.ONLY_ONE_IMAGE');
      return;
    } else {
      this.readfiles(files);
    }
  }

  removeBanner() {
    this.showRemoveButton = false;
    this.bannerFile = null;
    const image = document.getElementsByClassName('appendedImage')[0];
    const node = document.getElementById('imageDrop');
    if (!image) {
      node.removeChild(node.getElementsByTagName('img')[0]);
    } else {
      node.removeChild(node.getElementsByClassName('appendedImage')[0]);
    }
    this.storeService.removeStoreBanner(this.store.code)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('STORE_BRANDING.BANNER_REMOVED'));
      });
  }
}
