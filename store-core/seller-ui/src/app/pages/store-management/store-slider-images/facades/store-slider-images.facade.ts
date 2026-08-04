import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ErrorService} from '../../../shared/services/error.service';
import {sideMenuLinks} from '../../services/constents';
import {ReadableMerchantStoreWithPod} from '../../models/store-service.model';
import {SliderImage} from '../../models/store';

@Injectable()
export class StoreSliderImagesFacade {
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);

  readonly store = signal<ReadableMerchantStoreWithPod>(null);
  readonly newImageData = signal<string | ArrayBuffer | null>(null);
  readonly sliderImages = signal<SliderImage[]>([]);
  readonly loading = signal<boolean>(false);
  readonly selectedItem = signal<string>('4');
  readonly sideMenuLinks = sideMenuLinks;

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (!storeCode) return;

    this.storeService.getStore(storeCode).subscribe({
      next: (st) => {
        this.store.set(st);
        if (st.sliderImages && st.sliderImages.length > 0) {
          const sorted = [...st.sliderImages].sort((a: SliderImage, b: SliderImage) => a.priority - b.priority);
          sorted.forEach((it, i) => {
            it.priority = i;
          });
          this.sliderImages.set(sorted);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  route(link: string): void {
    const st = this.store();
    if (st) {
      this.router.navigate(['pages/store-management/' + link + '/', st.id]);
    }
  }

  save(): void {
    const imgs = this.sliderImages();
    const st = this.store();
    if (imgs && imgs.length > 0 && st) {
      let i = 0;
      const sliderImagesPayload = imgs.map(it => ({
        priority: i++,
        name: it.name,
        url: it.url
      }));
      this.storeService.saveStoreImageSliders(st.id, sliderImagesPayload).subscribe({
        next: () => {
          this.errorService.success('STORE.SLIDER_IMAGE_UPDATED');
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    }
  }

  allowDrop(e: DragEvent): void {
    e.preventDefault();
  }

  drop(e: DragEvent): void {
    e.preventDefault();
  }

  newImageChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.newImageData.set(e.target.result);
      };
      reader.readAsDataURL(target.files[0]);
    }
  }

  resetNewImage(newImageInput: HTMLInputElement): void {
    if (newImageInput) {
      newImageInput.value = '';
    }
    this.newImageData.set(null);
  }

  moveRight(index: number): void {
    const imgs = [...this.sliderImages()];
    if (index < imgs.length - 1) {
      const current = imgs[index];
      imgs[index] = imgs[index + 1];
      imgs[index + 1] = current;
      this.sliderImages.set(imgs);
    }
  }

  moveLeft(index: number): void {
    const imgs = [...this.sliderImages()];
    if (index > 0) {
      const storeImage: SliderImage = imgs[index];
      imgs[index] = imgs[index - 1];
      imgs[index - 1] = storeImage;
      this.sliderImages.set(imgs);
    }
  }

  deleteImage(i: number): void {
    const imgs = [...this.sliderImages()];
    imgs.splice(i, 1);
    this.sliderImages.set(imgs);
  }

  saveNewImage(newImageInput: HTMLInputElement): void {
    const st = this.store();
    if (!st || !newImageInput.files || !newImageInput.files[0]) return;

    this.storeService.addStoreSliderImage(st.id, newImageInput.files[0]).subscribe({
      next: (data) => {
        const imgs = [...this.sliderImages(), data];
        this.sliderImages.set(imgs);
        this.errorService.success('STORE.SLIDER_IMAGE_UPLOADED_SUCCESSFULLY');
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
      complete: () => {
        this.resetNewImage(newImageInput);
      }
    });
  }
}
