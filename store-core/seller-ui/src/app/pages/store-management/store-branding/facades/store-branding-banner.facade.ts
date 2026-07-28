import {Injectable, inject, signal} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {Banner} from '../../models/banner';

@Injectable()
export class StoreBrandingBannerFacade {
  private readonly formBuilder = inject(FormBuilder);
  private readonly storeService = inject(StoreService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly loadingButton = signal<boolean>(false);
  readonly showRemoveButton = signal<boolean>(false);
  readonly banner = signal<Banner | null>(null);

  acceptedImageTypes: Record<string, boolean> = {'image/png': true, 'image/jpeg': true, 'image/gif': true};
  bannerFile: any = null;

  imageUpload: FormGroup = this.formBuilder.group({
    imageInput: ['', Validators.required]
  });

  init(store: any): void {
    if (store && store.banner) {
      this.banner.set(store.banner);
      this.showRemoveButton.set(true);
    }
  }

  readfiles(files: any[], imageDropElement: any): void {
    this.bannerFile = files[0];
    this.showRemoveButton.set(true);
    const reader = new FileReader();
    const image = new Image();
    reader.onload = (event) => {
      if (imageDropElement) {
        imageDropElement.innerHTML = '';
        const fileReader = event.target as FileReader;
        image.src = fileReader.result as string;
        image.height = 200;
        image.style.display = 'block';
        image.style.margin = '0 auto';
        image.className = 'appendedImage';
        imageDropElement.appendChild(image);
      }
      if (this.imageUpload.controls['imageInput'].value == null) {
        const input = this.imageUpload.controls['imageInput'] as any;
        input.files = files;
      }
    };
    reader.readAsDataURL(files[0]);
  }

  allowDrop(e: DragEvent): void {
    e.preventDefault();
  }

  drop(e: DragEvent, imageDropElement: any): void {
    e.preventDefault();
    this.imageUpload.controls['imageInput'].reset();
    if (imageDropElement) {
      imageDropElement.innerHTML = '';
    }
    if (e.dataTransfer && e.dataTransfer.files) {
      this.checkfiles(Array.from(e.dataTransfer.files), imageDropElement);
    }
  }

  imageChange(event: any, imageDropElement: any): void {
    if (imageDropElement) {
      imageDropElement.innerHTML = '';
    }
    if (event.target.files) {
      this.checkfiles(Array.from(event.target.files), imageDropElement);
    }
  }

  checkfiles(files: any[], imageDropElement: any): void {
    if (!files || !files.length) return;
    if (this.acceptedImageTypes[files[0].type] !== true) {
      if (imageDropElement) {
        imageDropElement.innerHTML = this.translate.instant('STORE_BRANDING.NOT_AN_IMAGE');
      }
    } else if (files.length > 1) {
      if (imageDropElement) {
        imageDropElement.innerHTML = this.translate.instant('STORE_BRANDING.ONLY_ONE_IMAGE');
      }
    } else {
      this.readfiles(files, imageDropElement);
    }
  }

  saveBanner(storeId: string): void {
    this.loadingButton.set(true);
    this.storeService.addStoreBanner(storeId, this.bannerFile).subscribe({
      next: () => {
        this.errorService.success('STORE_BRANDING.BANNER_SAVED');
        this.loadingButton.set(false);
      },
      error: (err) => {
        this.loadingButton.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  removeBanner(storeId: string, imageDropElement: any): void {
    this.showRemoveButton.set(false);
    this.bannerFile = null;
    if (imageDropElement) {
      const image = imageDropElement.getElementsByClassName('appendedImage')[0];
      if (!image) {
        const imgTag = imageDropElement.getElementsByTagName('img')[0];
        if (imgTag) imageDropElement.removeChild(imgTag);
      } else {
        imageDropElement.removeChild(image);
      }
    }
    this.storeService.removeStoreBanner(storeId).subscribe({
      next: () => {
        this.errorService.success('STORE_BRANDING.BANNER_REMOVED');
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }
}
