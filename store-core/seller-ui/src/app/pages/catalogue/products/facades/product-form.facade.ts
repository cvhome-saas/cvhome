import {Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {NbRouteTab, NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {forkJoin} from 'rxjs';
import {ProductFormService} from '../services/product-form.service';
import {ProductService} from '../services/product.service';
import {ManufactureService} from '../../brands/services/manufacture.service';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {slugify} from '../../../shared/utils/slugifying';
import {PRODUCT_FORM_TABS} from '../constants/product-form.constants';

@Injectable()
export class ProductFormFacade {
  readonly formService = inject(ProductFormService);
  private readonly productService = inject(ProductService);
  private readonly manufactureService = inject(ManufactureService);
  private readonly configService = inject(ConfigService);
  private readonly storeService = inject(StoreService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly router = inject(Router);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly loaded = signal<boolean>(false);
  readonly tabLoader = signal<boolean>(false);
  readonly manufacturers = signal<{ value: string; label: string }[]>([]);
  readonly productTypes = signal<{ value: string; label: string }[]>([]);
  readonly languages = signal<any[]>([]);
  readonly defaultLanguage = signal<string>('');
  readonly currentLanguage = signal<string>('');
  readonly isCodeUnique = signal<boolean>(true);
  readonly tabs = signal<NbRouteTab[]>(
    PRODUCT_FORM_TABS.map((tab) => ({title: this.translate.instant(tab.titleKey), route: tab.route}))
  );

  private productData: any;

  init(product: any): void {
    this.productData = product;
    this.selectedStoreService.current().subscribe({
      next: (store) => this.loadFormContext(store),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  private loadFormContext(store: string): void {
    this.loader.set(true);

    forkJoin([
      this.storeService.getStore(store),
      this.manufactureService.getManufacturers(),
      this.productService.getProductTypes(),
      this.configService.getListOfSupportedLanguages(store)
    ]).subscribe({
      next: ([storeData, manufacturers, productTypes, languages]) => {
        this.defaultLanguage.set(storeData.defaultLanguage);
        this.currentLanguage.set(storeData.defaultLanguage);
        this.manufacturers.set(manufacturers.content.map((option: any) => ({value: option.code, label: option.code})));
        this.productTypes.set(productTypes.content.map((option: any) => ({value: option.code, label: option.code})));
        this.languages.set([...languages]);

        this.formService.initDescriptions(languages);
        this.formService.selectLanguage(storeData.defaultLanguage);

        if (this.productData?.id) {
          this.formService.patchForm(this.productData, storeData.defaultLanguage);
        }

        this.loader.set(false);
        this.loaded.set(true);
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  selectLanguage(langCode: string): void {
    this.formService.selectLanguage(langCode);
    this.currentLanguage.set(langCode);
  }

  changeName(name: string, index: number): void {
    this.formService.descriptions.at(index).patchValue({
      friendlyUrl: slugify(name)
    });
  }

  checkSku(sku: string): void {
    this.loader.set(true);
    this.productService.checkProductSku(sku).subscribe({
      next: (res) => {
        this.isCodeUnique.set(!(res.exists && this.productData?.sku !== sku));
        this.loader.set(false);
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  save(): void {
    this.formService.form.markAllAsTouched();
    this.loader.set(true);

    const productObject = this.formService.form.value;

    const tmpObj: any = {name: '', friendlyUrl: '', title: '', language: ''};
    productObject.descriptions.forEach((el: any) => {
      tmpObj.language = el.language;
      if (tmpObj.name === '' && el.name !== '') {
        tmpObj.name = el.name;
      }
      if (tmpObj.friendlyUrl === '' && el.friendlyUrl !== '') {
        tmpObj.friendlyUrl = el.friendlyUrl;
      }
      if (tmpObj.title === '' && el.title !== '') {
        tmpObj.title = el.title;
      }
      if (tmpObj.title === '' && el.title === '') {
        tmpObj.title = el.name;
      }
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (!tmpObj.hasOwnProperty(elKey) && el[elKey] !== '') {
            tmpObj[elKey] = el[elKey];
          }
        }
      }
    });

    if (this.findInvalidControls().length > 0) {
      this.loader.set(false);
      return;
    }

    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || productObject.sku === '' || productObject.manufacturer === '') {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
      this.loader.set(false);
      return;
    }

    productObject.descriptions.forEach((el: any) => {
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (el[elKey] === '' && tmpObj[elKey] !== '') {
            el[elKey] = tmpObj[elKey];
          }
        }
      }
    });

    productObject.descriptions.forEach((el: any) => {
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (typeof el[elKey] === 'undefined' || !el[elKey]) {
            el.name = el.name.trim();
            el[elKey] = '';
          }
        }
      }
    });

    delete productObject.selectedLanguage;

    const request$ = this.productData?.id
      ? this.productService.updateProduct(this.productData.id, productObject)
      : this.productService.createProduct(productObject);

    request$.subscribe({
      next: () => {
        this.loader.set(false);
        this.toastr.success(this.translate.instant(
          this.productData?.id ? 'PRODUCT.PRODUCT_UPDATED' : 'PRODUCT.PRODUCT_CREATED'
        ));
        if (!this.productData?.id) {
          this.goToBack();
        }
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        if (!this.productData?.id && err?.error?.message) {
          this.toastr.danger(err.error.message);
        }
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/catalogue/products/products-list']);
  }

  private findInvalidControls(): string[] {
    const invalid: string[] = [];
    const controls = this.formService.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    if (invalid.length > 0) {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS') + ' [' + invalid + ' ]');
    }
    return invalid;
  }
}
