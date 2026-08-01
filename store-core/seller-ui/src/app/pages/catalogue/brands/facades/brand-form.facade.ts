import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Router} from '@angular/router';
import {BrandFormService} from '../services/brand-form.service';
import {BrandService} from '../services/brand.service';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from '../../../shared/services/error.service';
import {slugify} from '../../../shared/utils/slugifying';
import {Store} from '../../../store-management/models/store';
import {SupportedLanguageCode} from '../../../shared/services/config.service';
import {ManufacturerDescription, ReadableManufacturer} from '../models/brand.model';

@Injectable()
export class BrandFormFacade {
  readonly formService = inject(BrandFormService);
  private readonly brandService = inject(BrandService);
  private readonly configService = inject(ConfigService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly isCodeUnique = signal<boolean>(true);
  readonly languages = signal<SupportedLanguageCode[]>([]);
  readonly defaultLanguage = signal<string>('');

  private brandData: ReadableManufacturer;
  private storeData: Store;

  init(brand: ReadableManufacturer, store: Store, destroyRef: DestroyRef): void {
    this.brandData = brand || {};
    this.storeData = store;
    if (!store) return;

    this.defaultLanguage.set(store.defaultLanguage || 'en');
    this.loader.set(true);

    this.configService.getListOfSupportedLanguages(store.id)
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe({
      next: (res) => {
        this.languages.set([...res]);
        if (this.brandData.id) {
          this.formService.patchForm(this.brandData, res, this.defaultLanguage());
        } else {
          this.formService.form.patchValue({selectedLanguage: this.defaultLanguage()});
          this.formService.initDescriptions(res);
        }
        this.loader.set(false);
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  changeName(event: string, index: number): void {
    this.formService.descriptions.at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  checkCode(event: Event): void {
    const code = (event.target as HTMLInputElement).value;
    if (!code) return;

    this.brandService.checkBrandCode(code).subscribe({
      next: (res) => {
        this.isCodeUnique.set(!(res.exists && (this.brandData.code !== code)));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  save(): void {
    const brandObject = this.formService.form.value;
    const tmpObj: Record<string, string> = {
      name: '',
      friendlyUrl: ''
    };

    brandObject.descriptions.forEach((el: ManufacturerDescription) => {
      if (tmpObj.name === '' && el.name !== '') {
        tmpObj.name = el.name;
      }
      if (tmpObj.friendlyUrl === '' && el.friendlyUrl !== '') {
        tmpObj.friendlyUrl = el.friendlyUrl;
      }
      for (const elKey in el) {
        if (Object.prototype.hasOwnProperty.call(el, elKey)) {
          if (!Object.prototype.hasOwnProperty.call(tmpObj, elKey) && el[elKey] !== '') {
            tmpObj[elKey] = el[elKey];
          }
        }
      }
    });

    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || brandObject.code === '') {
      this.errorService.error('COMMON.FILL_REQUIRED_FIELDS', null);
      return;
    }

    brandObject.descriptions.forEach((el: ManufacturerDescription) => {
      for (const elKey in el) {
        if (Object.prototype.hasOwnProperty.call(el, elKey)) {
          if (el[elKey] === '' && tmpObj[elKey] !== '') {
            el[elKey] = tmpObj[elKey];
          }
        }
      }
    });

    brandObject.descriptions.forEach((el: ManufacturerDescription) => {
      for (const elKey in el) {
        if (Object.prototype.hasOwnProperty.call(el, elKey)) {
          if (el.name) {
            el.name = el.name.trim();
          }
          if (typeof el[elKey] === 'undefined') {
            el[elKey] = '';
          }
        }
      }
    });

    if (!this.isCodeUnique()) {
      this.errorService.error('COMMON.CODE_EXISTS', null);
      return;
    }

    this.loader.set(true);

    if (this.brandData.id) {
      this.brandService.updateBrand(this.brandData.id, brandObject).subscribe({
        next: () => {
          this.loader.set(false);
          this.errorService.success('BRAND.BRAND_UPDATED');
        },
        error: (err) => {
          this.loader.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    } else {
      this.brandService.createBrand(brandObject).subscribe({
        next: () => {
          this.loader.set(false);
          this.errorService.success('BRAND.BRAND_CREATED');
          this.router.navigate(['pages/catalogue/brands/brands-list']);
        },
        error: (err) => {
          this.loader.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    }
  }

  goToBack(): void {
    this.router.navigate(['pages/catalogue/brands/brands-list']);
  }
}
