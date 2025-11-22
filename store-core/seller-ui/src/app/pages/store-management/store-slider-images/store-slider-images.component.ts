import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";

@Component({
  selector: 'ngx-store-slider-images',
  standalone: false,
  templateUrl: './store-slider-images.component.html',
  styleUrls: ['./store-slider-images.component.scss']
})
export class StoreSliderImagesComponent implements OnInit {
  store;
  newImageData: string | ArrayBuffer | null;
  sliderImages: SliderImage[] = [];
  loading = false;
  selectedItem = '4';
  sidemenuLinks = [
    {
      id: '0',
      title: 'Store branding',
      key: 'COMPONENTS.STORE_BRANDING',
      link: 'store-branding'
    },
    {
      id: '1',
      title: 'Store home page',
      key: 'COMPONENTS.STORE_LANDING',
      link: 'store-landing'
    },
    {
      id: '2',
      title: 'Store domain',
      key: 'COMPONENTS.STORE_DOMAIN',
      link: 'store-domain'
    },
    {
      id: '3',
      title: 'Store Social Links',
      key: 'COMPONENTS.STORE_SOCIAL_LINKS',
      link: 'store-social-links'
    },
    {
      id: '4',
      title: 'Store Slider Images',
      key: 'COMPONENTS.STORE_SLIDER_IMAGES',
      link: 'store-slider-images'
    },
    {
      id: '5',
      title: 'Store details',
      key: 'COMPONENTS.STORE_DETAILS',
      link: 'store'
    }
  ];

  constructor(
    private storeService: StoreService,
    private fb: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
  }


  ngOnInit() {
    this.storeService.getStore(this.activatedRoute.snapshot.paramMap.get('code'))
      .subscribe({
        next: (store) => {
          this.store = store;
          if (this.store.sliderImages && this.store.sliderImages.length > 0) {
            this.sliderImages = this.store.sliderImages.sort((a: SliderImage, b: SliderImage) => a.priority - b.priority) as SliderImage[];
            this.sliderImages
              .forEach((it, i) => {
                it.priority = i;
              });
            console.log(this.sliderImages)
          }

        },
        error: (err) => {
          this.loading = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }

  save() {
    if (this.sliderImages && this.sliderImages.length > 0) {
      let i = 0;
      let sliderImages = this.sliderImages.map(it => {
        return {
          priority: i++,
          name: it.name,
          url: it.url
        };
      });
      this.storeService.saveStoreImageSliders(this.store.id, sliderImages)
        .subscribe({
          next: (data) => {
            this.toastr.success(this.translate.instant('STORE.SLIDER_IMAGE_UPDATED'));
          },
          error: (err) => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);

          },
          complete: () => {
          }
        })
    }
  }


  allowDrop(e) {
    e.preventDefault();
  }

  drop(e) {
    e.preventDefault();
  }

  // imageChange
  imageChange(event) {
  }

  newImageChange(event) {
    if (event.target.files && event.target.files[0]) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.newImageData = e.target.result;
      };
      reader.readAsDataURL(event.target.files[0]);
    }
  }

  resetNewImage(newImageInput: HTMLInputElement) {
    newImageInput.value = '';
    this.newImageData = null;
  }

  moveRight(index: number) {
    if (index < this.sliderImages.length - 1) {
      const current = this.sliderImages[index];
      this.sliderImages[index] = this.sliderImages[index + 1];
      this.sliderImages[index + 1] = current;
    }
  }

  moveLeft(index: number) {
    if (index > 0) {
      const storeImage: SliderImage = this.sliderImages[index];
      this.sliderImages[index] = this.sliderImages[index - 1];
      this.sliderImages[index - 1] = storeImage;
    }
  }

  deleteImage(i: number) {
    this.sliderImages.splice(i, 1);
  }

  saveNewImage(newImageInput: HTMLInputElement) {
    this.storeService.addStoreSliderImage(this.store.id, newImageInput.files[0])
      .subscribe({
        next: (data) => {
          this.sliderImages.push(data)
          this.toastr.success(this.translate.instant('STORE.SLIDER_IMAGE_UPLOADED_SUCCESSFULLY'));
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

export interface SliderImage {
  priority: number;
  name: string;
  url: string;
}
