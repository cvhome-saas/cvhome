import {Injectable, inject, signal} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StoreService} from '../../services/store.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {Logo} from '../../models/store';
import {ReadableMerchantStoreWithPod} from '../../models/store-service.model';

@Injectable()
export class StoreBrandingLogoFacade {
  private readonly formBuilder = inject(FormBuilder);
  private readonly storeService = inject(StoreService);
  private readonly translate = inject(TranslateService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly loadingButton = signal<boolean>(false);
  readonly showRemoveButton = signal<boolean>(false);
  readonly logo = signal<Logo | null>(null);

  acceptedImageTypes: Record<string, boolean> = {'image/png': true, 'image/jpeg': true, 'image/gif': true};
  logoFile: File = null;

  imageUpload: FormGroup = this.formBuilder.group({
    imageInput: ['', Validators.required]
  });

  init(store: ReadableMerchantStoreWithPod): void {
    if (store && store.logo) {
      this.logo.set(store.logo);
      this.showRemoveButton.set(true);
    }
  }

  readfiles(files: File[], imageDropElement: HTMLElement): void {
    this.logoFile = files[0];
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
        const input = this.imageUpload.controls['imageInput'] as FormControl & {files?: File[]};
        input.files = files;
      }
    };
    reader.readAsDataURL(files[0]);
  }

  allowDrop(e: DragEvent): void {
    e.preventDefault();
  }

  drop(e: DragEvent, imageDropElement: HTMLElement): void {
    e.preventDefault();
    this.imageUpload.controls['imageInput'].reset();
    if (imageDropElement) {
      imageDropElement.innerHTML = '';
    }
    if (e.dataTransfer && e.dataTransfer.files) {
      this.checkfiles(Array.from(e.dataTransfer.files), imageDropElement);
    }
  }

  imageChange(event: Event, imageDropElement: HTMLElement): void {
    if (imageDropElement) {
      imageDropElement.innerHTML = '';
    }
    const target = event.target as HTMLInputElement;
    if (target.files) {
      this.checkfiles(Array.from(target.files), imageDropElement);
    }
  }

  checkfiles(files: File[], imageDropElement: HTMLElement): void {
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

  saveLogo(storeId: string): void {
    this.loadingButton.set(true);
    this.storeService.addStoreLogo(storeId, this.logoFile).subscribe({
      next: () => {
        this.notify.success('STORE_BRANDING.LOGO_SAVED');
        this.loadingButton.set(false);
      },
      error: (err) => {
        this.loadingButton.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  removeLogo(storeId: string, imageDropElement: HTMLElement): void {
    this.showRemoveButton.set(false);
    this.logoFile = null;
    if (imageDropElement) {
      const image = imageDropElement.getElementsByClassName('appendedImage')[0];
      if (!image) {
        const imgTag = imageDropElement.getElementsByTagName('img')[0];
        if (imgTag) imageDropElement.removeChild(imgTag);
      } else {
        imageDropElement.removeChild(image);
      }
    }
    this.storeService.removeStoreLogo(storeId).subscribe({
      next: () => {
        this.notify.success('STORE_BRANDING.LOGO_REMOVED');
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });
  }
}
