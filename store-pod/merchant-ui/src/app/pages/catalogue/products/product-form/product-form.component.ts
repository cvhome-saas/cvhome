import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import {ManufactureService} from '../../brands/services/manufacture.service';
import {ConfigService} from '../../../../shared/services/config.service';
import moment from 'moment';
import {NbDialogService, NbRouteTab, NbToastrService} from '@nebular/theme';
import {ProductService} from '../services/product.service';
import {TranslateService} from '@ngx-translate/core';
import {validators} from '../../../../shared/validation/validators';
import {slugify} from '../../../../shared/utils/slugifying';
import {forkJoin} from 'rxjs';
import {ImageBrowserComponent} from "../../../../shared/components/image-browser/image-browser.component";
import {ErrorService} from "../../../../shared/services/error.service";

declare var $: any;

@Component({
  selector: 'ngx-product-form',
  standalone: false,
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  @Input() product: any;
  @Input() _title: string;
  store: string;
  action: any = 'save'
  uniqueCode: string;
  form: FormGroup;
  loaded = false;
  tabLoader = false;
  loader = false;
  manufacturers = [];
  languages = [];
  productTypes = [];
  defaultLanguage: string;
  currentLanguage: string;


  tabs: NbRouteTab[] ;

  isCodeUnique = true;

  constructor(
    private fb: FormBuilder,
    private manufactureService: ManufactureService,
    private configService: ConfigService,
    private toastr: NbToastrService,
    private productService: ProductService,
    private errorService: ErrorService,
    private router: Router,
    private translate: TranslateService,
    private dialogService: NbDialogService,
    private activatedRoute: ActivatedRoute,
  ) {
    this.defaultLanguage = this.translate.defaultLang
    this.currentLanguage = this.translate.currentLang
    this.tabs = [
      {
        title: this.translate.instant('COMPONENTS.PRODUCTS_IMAGES'),
        route: 'images',
      },
      {
        title: this.translate.instant('COMPONENTS.PRODUCT_TO_CATEGORY'),
        route: 'category',
      },
      {
        title: this.translate.instant('COMPONENTS.PRODUCT_RELATED'),
        route: 'related',
      },
      {
        title: this.translate.instant('COMPONENTS.OPTIONS_CONFIG'),
        route: 'options',
      },
      {
        title: this.translate.instant('COMPONENTS.PRODUCTS_PROPERTIES'),
        route: 'properties',
      },
      {
        title: this.translate.instant('COMPONENTS.PRODUCTS_DISCOUNT'),
        route: 'discount',
      }
    ];
  }

  get sku() {
    return this.form.get('sku');
  }

  get manufacturer() {
    return this.form.get('manufacturer');
  }

  get price() {
    return this.form.get('price');
  }

  get quantity() {
    return this.form.get('quantity');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  ngOnInit() {


    this.activatedRoute.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.action = 'edit';
        this.uniqueCode = split[1];
      }
      this.loadssss();
    });


  }

  addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          name: ['', [Validators.required]],
          highlights: [''],
          friendlyUrl: ['', [Validators.required]],
          description: [''],
          title: [''],
          keyWords: [''],
          metaDescription: [''],
        })
      );
    });
  }

  fillForm() {
    this.form.patchValue({
      sku: this.product.sku,
      visible: this.product.visible,
      canBePurchased: this.product.canBePurchased,
      dateAvailable: new Date(this.product.dateAvailable),
      manufacturer: this.product.manufacturer == null ? '' : this.product.manufacturer.code,
      type: this.product.type == null ? '' : this.product.type.code,
      price: this.product.inventory.price,
      quantity: this.product.inventory.quantity,
      productSpecifications: {
        weight: this.product.productSpecifications.weight,
        height: this.product.productSpecifications.height,
        width: this.product.productSpecifications.width,
        length: this.product.productSpecifications.length
      },
      sortOrder: this.product.sortOrder,
      // productShipeable: this.product.productShipeable,
      // placementOrder: [0, [Validators.required]],  // ???
      // taxClass: [0, [Validators.required]], // ???
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();

    //this.findInvalidControls();

    // const dimension = {
    //   weight: this.product.productSpecifications.weight,
    //   height: this.product.productSpecifications.height,
    //   width: this.product.productSpecifications.width,
    //   length: this.product.productSpecifications.length,
    // };
    // this.form.patchValue({ productSpecifications: dimension });
  }

  fillFormArray() {
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.product != null && this.product.descriptions) {
        this.product.descriptions.forEach((description) => {
          if (desc.language === description.language) {
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              language: description.language,
              name: description.name,
              highlights: description.highlights,
              friendlyUrl: description.friendlyUrl,
              description: description.description,
              title: description.title,
              keyWords: description.keyWords,
              metaDescription: description.metaDescription,
            });
          }
        });
      }
    });
  }

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
    this.currentLanguage = lang;
  }

  changeName(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  checkSku(event) {
    this.loader = true;
    this.productService.checkProductSku(this.store, event.target.value)
      .subscribe(res => {
        this.isCodeUnique = !(res.exists && (this.product.sku !== event.target.value));
        this.loader = false;
      }, err => {
        this.loader = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  save() {
    this.form.markAllAsTouched();

    this.loader = true;
    const productObject = this.form.value;
    productObject.dateAvailable = moment(productObject.dateAvailable).format('yyyy-MM-DD');
    // productObject.productSpecifications.manufacturer = productObject.manufacturer;

    // save important values for filling empty field in result object
    const tmpObj = {
      name: '',
      friendlyUrl: '',
      title: '',
      language: ''
    };
    productObject.descriptions.forEach((el) => {
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
    /** do the validation */
    if (this.findInvalidControls().length > 0) {
      this.loader = false;
      return;
    }
    // check required fields
    //object validations on the form
    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || productObject.sku === '' || productObject.manufacturer === '') {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
      this.loader = false;
    } else {
      productObject.descriptions.forEach((el) => {
        // fill empty fields
        for (const elKey in el) {
          if (el.hasOwnProperty(elKey)) {
            if (el[elKey] === '' && tmpObj[elKey] !== '') {
              el[elKey] = tmpObj[elKey];
            }
          }
        }
      });
      // check for undefined
      productObject.descriptions.forEach(el => {
        for (const elKey in el) {
          if (el.hasOwnProperty(elKey)) {
            if (typeof el[elKey] === 'undefined' || !el[elKey]) {
              el.name = el.name.trim(); // trim name
              el[elKey] = '';
            }
          }
        }
      });
      delete productObject.selectedLanguage;
      if (this.product.id) {
        this.productService.updateProduct(this.store, this.product.id, productObject)
          .subscribe({
            next: (data) => {
              this.loader = false;
              this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
            },
            error: (err) => {
              this.errorService.error('ERROR.SYSTEM_ERROR', err);
              this.loader = false;
            }
          });
      } else {
        this.productService.createProduct(this.store, productObject)
          .subscribe({
            next: (data) => {
              this.loader = false;
              this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_CREATED'));
              this.router.navigate(['pages/catalogue/products/products-list']);
            },
            error: (err) => {
              this.errorService.error('ERROR.SYSTEM_ERROR', err);
              this.toastr.danger(err.error.message);
              this.loader = false;
            }
          });
      }
    }
  }

  route(link) {
    this.router.navigate(['pages/catalogue/products/' + this.product.id + '/' + link]);
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/products/products-list'])
  }

  public findInvalidControls() {
    const invalid = [];
    const controls = this.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    if (invalid.length > 0) {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS') + " [" + invalid + " ]");
    }
    return invalid;
  }

  private loadEvent() {
    this.loader = true;
    this.loaded = false;
  }

  private loadedEvent() {
    this.loader = false;
    this.loaded = true;
  }

  private createForm() {
    this.form = this.fb.group({
      sku: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      dateAvailable: [new Date()],
      manufacturer: ['', [Validators.required]],
      type: [''],
      display: [true],
      canBePurchased: [true],
      timeBound: [false],
      price: ['', [Validators.required]],
      quantity: ['', [Validators.required, Validators.pattern(validators.number)]],
      // discountedPrice: [Validators.pattern(validators.number)],
      // percentageOff: [Validators.pattern(validators.number)],
      // rebatePrice: [Validators.required, Validators.pattern(validators.number)],
      // startDate: [new Date()],
      // endDate: [new Date()],
      sortOrder: ['', [Validators.required, Validators.pattern(validators.number)]],
      // productShipeable: [false, [Validators.required]],
      productSpecifications: this.fb.group({
        weight: [''],
        height: [''],
        width: [''],
        length: ['']
      }),
      // placementOrder: [0, [Validators.required]],  // ???
      // taxClass: [0, [Validators.required]], // ???
      selectedLanguage: [this.defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private loadssss() {
    this.loadEvent();

    const manufacture = this.manufactureService.getManufacturers(this.store);
    const types = this.productService.getProductTypes(this.store);
    const config = this.configService.getListOfSupportedLanguages(this.store);
    forkJoin([manufacture, types, config])
      .subscribe(([manufacturers, productTypes, languages]) => {

        manufacturers.manufacturers.forEach((option) => {
          this.manufacturers.push({value: option.code, label: option.code});
        });

        productTypes.list.forEach((option) => {
          this.productTypes.push({value: option.code, label: option.code});
        });

        this.languages = [...languages];
        this.createForm();//init
        this.addFormArray();//create array
        if (this.product.id) {
          this.fillForm();//bind content to the form
        }
        this.loadedEvent();
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });

  }
}
