import {ChangeDetectorRef, Component, Input, NgZone, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import {ConfigService} from '../../shared/services/config.service';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {validators} from '../../shared/validation/validators';
import {forkJoin} from 'rxjs';
import {CrudService} from "../../shared/services/crud.service";

@Component({
  selector: 'ngx-store-form',
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.scss']
})
export class StoreFormComponent implements OnInit {
  @Input() title: string;
  @Input() store: any;
  @Input() isCancel: string;

  supportedLanguages = [];
  supportedLanguagesSelected = [];
  supportedCurrency = [];
  weightList = [];
  sizeList = [];
  countries = [];
  form: FormGroup;
  loading = false;
  isReadonlyName = false;
  isNameUnique = true;
  establishmentType = 'STORE';
  selectedItem = '2';
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
      title: 'Store details',
      key: 'COMPONENTS.STORE_DETAILS',
      link: 'store'
    }
  ];

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService,
    private storeService: StoreService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService) {
  }

  ngOnInit() {
    this.loading = true;
    forkJoin(
      this.configService.getListOfCountries(),
      this.configService.getListOfSupportedCurrency(),
      this.configService.getWeightAndSizes())
      .subscribe(([countries, currencies, measures]) => {
        this.countries = [...countries];
        this.supportedCurrency = [...currencies];
        this.weightList = [...measures.weights];
        this.sizeList = [...measures.measures];
        this.supportedLanguages = this.configService.getListOfGlobalLanguages();
        this.loading = false;
      });


    this.createForm();
  }

  private createForm() {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      phone: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.pattern(validators.emailPattern)]],
      address: this.fb.group({
        searchControl: [''],
        stateProvince: [{value: '', disabled: false}],
        country: ['', [Validators.required]],
        address: ['', [Validators.required]],
        postalCode: ['', [Validators.required]],
        city: ['', [Validators.required]]
      }),
      supportedLanguages: [[], [Validators.required]],
      defaultLanguage: ['', [Validators.required]],
      currency: [''],
      currencyFormatNational: [true],
      weight: ['', [Validators.required]],
      dimension: ['', [Validators.required]],
      inBusinessSince: [new Date()],
      useCache: [false],
    });

    if (this.store && this.store.id > 0) {
      this.fillForm();
    }

  }


  fillForm() {

    this.store.supportedLanguages.forEach(lang => {
      this.supportedLanguagesSelected.push(lang.code);
    });
    this.form.patchValue({
      name: this.store.name,
      code: this.store.code,
      phone: this.store.phone,
      email: this.store.email,
      supportedLanguages: this.store.supportedLanguages,
      defaultLanguage: this.store.defaultLanguage,
      currency: this.store.currency,
      currencyFormatNational: this.store.currencyFormatNational,
      weight: this.store.weight,
      dimension: this.store.dimension,
      inBusinessSince: new Date(this.store.inBusinessSince),
      useCache: this.store.useCache,
    });
    this.form.controls['address'].patchValue({searchControl: ''});
    this.form.controls['address'].patchValue({stateProvince: this.store.address.stateProvince}, {disabled: false});
    this.form.controls['address'].patchValue({country: this.store.address.country});
    this.form.controls['address'].patchValue({address: this.store.address.address});
    this.form.controls['address'].patchValue({postalCode: this.store.address.postalCode});
    this.form.controls['address'].patchValue({city: this.store.address.city});
    this.isReadonlyName = true;
    this.cdr.markForCheck();

  }

  get name() {
    return this.form.get('name');
  }

  get code() {
    return this.form.get('code');
  }

  get phone() {
    return this.form.get('phone');
  }

  get address() {
    return this.form.get('address').get('address');
  }

  get stateProvince() {
    return this.form.get('address').get('stateProvince');
  }

  get country() {
    return this.form.get('address').get('country');
  }

  get city() {
    return this.form.get('address').get('city');
  }

  get postalCode() {
    return this.form.get('address').get('postalCode');
  }

  get email() {
    return this.form.get('email');
  }

  get inBusinessSince() {
    return this.form.get('inBusinessSince');
  }


  save() {
    //this.findInvalidControls();
    this.form.controls['address'].patchValue({country: this.form.value.address.country});
    this.form.controls['address'].patchValue({stateProvince: this.form.value.address.stateProvince});
    const storeObj = this.form.value;


    storeObj.supportedLanguages = this.supportedLanguagesSelected;

    if (this.store && this.store.id) {
      this.storeService.updateStore(storeObj)
        .subscribe(store => {
          this.toastr.success(this.translate.instant('STORE_FORM.STORE_UPDATED'));
          this.router.navigate(['pages/store-management/stores-list']);
        });
    } else {
      this.storeService.checkIfStoreExist(this.form.value.name)
        .subscribe(res => {
          if (res.exist) {
            this.toastr.success(this.translate.instant('COMMON.NAME_EXISTS'));
          } else {
            this.storeService.createStore(storeObj)
              .subscribe(store => {
                this.toastr.success(this.translate.instant('STORE_FORM.STORE_CREATED'));
                this.router.navigate(['pages/store-management/stores-list']);
              });
          }
        });
    }
  }


  addSupportedLanguage(languageCode) {
    let newLanguages = this.form.value.supportedLanguages ? [...this.form.value.supportedLanguages] : [];
    // check if element is exist in array
    const index = newLanguages.indexOf(languageCode);
    const selectedIndex = this.supportedLanguagesSelected.indexOf(languageCode);

    // if exist
    if (selectedIndex !== -1) {//already exist, remove element at index
      this.supportedLanguagesSelected.splice(selectedIndex, 1);
    } else {//add language
      this.supportedLanguagesSelected.push(languageCode);
    }

    this.form.patchValue({'supportedLanguages': this.supportedLanguagesSelected}); // rewrite form

  }


  userHasSupportedLanguage(language) {
    if (!this.store || !this.store.supportedLanguages)
      return false;
    return this.store.supportedLanguages.find((l: any) => l.code === language.code);
  }


  checkName(event) {
    this.storeService.checkIfStoreExist(this.form.value.name)
      .subscribe(res => {
        this.isNameUnique = !res.exists
      });
  }

  onClickRoute(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.code]);
  }

  goToBack() {
    this.router.navigate(['pages/store-management/stores-list']);
  }

}
