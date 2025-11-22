import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, Validators} from "@angular/forms";
import {TranslateService} from "@ngx-translate/core";
import {StoreService} from "../services/store.service";
import {NbToastrService} from "@nebular/theme";
import {Logo} from "../models/logo";
import {ErrorService} from "../../shared/services/error.service";

@Component({
  selector: 'ngx-store-branding-logo',
  standalone:false,
  template: `
    <form [formGroup]="imageUpload">
      <div (click)='imageInput.click()' (dragover)="allowDrop($event)" (drop)="drop($event)" class="wrapper">
        <div #imageDrop id="imageDrop">

          <img *ngIf="logo" [src]="logo?.path" style="height:250px"/>

        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input nbInput fullWidth #imageInput (change)='imageChange($event)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        <button nbButton status="danger" (click)="removeLogo()" *ngIf="showRemoveButton"  type="submit">{{
            'COMMON.REMOVE' | translate
          }}
        </button>
        <button nbButton status="success" (click)="saveLogo()" *ngIf="logoFile" [nbSpinner]="loadingButton"
                nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
          {{ (!loadingButton ? 'COMMON.SAVE' : '') | translate }}
        </button>
      </div>

    </form>
  `,
  styleUrls: ['./store-branding.component.scss']
})
export class StoreBrandingLogoComponent implements OnInit {
  @Input()
  store: any
  @ViewChild('imageDrop', {static: false}) imageDrop;
  acceptedImageTypes = {'image/png': true, 'image/jpeg': true, 'image/gif': true};
  imageUpload;
  loadingButton = false;
  logo: Logo;

  logoFile: any;
  showRemoveButton = false;

  constructor(private formBuilder: FormBuilder,
              private storeService: StoreService,
              private toastr: NbToastrService,
              private translate: TranslateService,
              private errorService: ErrorService
  ) {
    this.imageUpload = this.formBuilder.group({
      imageInput: ['', Validators.required]
    });

  }

  ngOnInit(): void {

    this.logo = this.store.logo;
    if (this.logo) {
      this.showRemoveButton = true;
    }
  }


  // readfiles
  readfiles(files) {
    this.logoFile = files[0];
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

  saveLogo() {
    this.loadingButton = true;
    this.storeService.addStoreLogo(this.store.id, this.logoFile)
      .subscribe({
        next: (data) => {
          this.toastr.success(this.translate.instant('STORE_BRANDING.LOGO_SAVED'));
          this.loadingButton = false;
        },
        error: (err) => {
          this.loadingButton = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
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

  removeLogo() {
    this.showRemoveButton = false;
    this.logoFile = null;
    const image = document.getElementsByClassName('appendedImage')[0];
    const node = document.getElementById('imageDrop');
    if (!image) {
      node.removeChild(node.getElementsByTagName('img')[0]);
    } else {
      node.removeChild(node.getElementsByClassName('appendedImage')[0]);
    }
    this.storeService.removeStoreLogo(this.store.id)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('STORE_BRANDING.LOGO_REMOVED'));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }
}
