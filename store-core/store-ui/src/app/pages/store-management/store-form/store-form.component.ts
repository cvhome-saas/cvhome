import {ChangeDetectorRef, Component, ElementRef, Input, NgZone, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';

import {ConfigService} from '../../shared/services/config.service';
import {environment} from '../../../../environments/environment';
import {StoreService} from '../services/store.service';
import {UserService} from '../../shared/services/user.service';
import {SecurityService} from '../../shared/services/security.service';
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

  @ViewChild('search', {static: false})
  searchElementRef: ElementRef;
  supportedLanguages = [];
  supportedLanguagesSelected = [];
  supportedCurrency = [];
  weightList = [];
  sizeList = [];
  flag = true;
  provinces = [];
  countries = [];
  form: FormGroup;
  env = environment;
  componentForm = {
    street_number: 'short_name',
    route: 'long_name',
    locality: 'long_name',
    administrative_area_level_1: 'short_name',
    administrative_area_level_2: 'short_name',
    country: 'short_name',
    postal_code: 'short_name',
    sublocality_level_1: 'long_name'
  };
  loading = false;
  showRemoveButton = true;
  isReadonlyCode = false;
  isSuperadmin: boolean;
  roles: any = {};
  isCodeUnique = true;
  establishmentType = 'STORE';
  parent: any;
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
    private userService: UserService,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private activatedRoute: ActivatedRoute,
    private securityService: SecurityService,
    private crudService: CrudService
  ) {
  }

  ngOnInit() {
    this.roles = JSON.parse(localStorage.getItem('roles'));
    this.loading = true;
    forkJoin(
      this.getListOfCountries(),
      this.getListOfSupportedCurrency(),
      this.getWeightAndSizes())
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
      name: ['', [Validators.required]],
      code: [{value: '', disabled: false}, [Validators.required, Validators.pattern(validators.alphanumeric)]],
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

    //console.log('Creating form 3 ');
    //console.log('Store id' + this.store.id);
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
    if (this.store.address.country) {
      this.countryIsSelected(this.store.address.country);
    }
    this.isReadonlyCode = true;
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
          this.toastr.success(this.translate.instant('STORE_FORM.' + this.establishmentType + '_UPDATED'));
          this.router.navigate(['pages/store-management/stores-list']);
        });
    } else {
      this.storeService.checkIfStoreExist(this.form.value.code)
        .subscribe(res => {
          if (res.exist) {
            this.toastr.success(this.translate.instant('COMMON.CODE_EXISTS'));
          } else {
            this.storeService.createStore(storeObj)
              .subscribe(store => {
                this.toastr.success(this.translate.instant('STORE_FORM.' + this.establishmentType + '_CREATED'));
                this.router.navigate(['pages/store-management/stores-list']);
              });
          }
        });
    }
  }

  countryIsSelected(code) {
    this.provinces = [];
    // this.stateProvince.disable();
    this.getListOfZonesProvincesByCountry(code)
      .subscribe({
        next: (data) => {
          this.provinces = [...data];
          if (this.provinces.length > 0) {
            // this.stateProvince.enable();
          }

        },
        error: (err) => {
          this.toastr.success(this.translate.instant('STORE_FORM.ERROR_STATE_PROVINCE'));

        },
      });
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

  public findInvalidControls() {
    const invalid = [];
    const controls = this.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    //console.log('Invalid fields ' + invalid);
  }

  userHasSupportedLanguage(language) {
    if (!this.store || !this.store.supportedLanguages)
      return false;
    return this.store.supportedLanguages.find((l: any) => l.code === language.code);
  }


  checkCode(event) {
    const code = event.target.value;
    this.storeService.checkIfStoreExist(this.form.value.code)
      .subscribe(res => {
        this.isCodeUnique = !(res.exists && (this.store.code !== code));
      });
  }

  onClickRoute(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.code]);
  }

  goToBack() {
    this.router.navigate(['pages/store-management/stores-list']);
  }

  getListOfSupportedCurrency() {
    return this.crudService.get(`/store/api/v1/currency?store=${this.store.code}`);
  }

  getWeightAndSizes() {
    return this.crudService.get(`/store/api/v1/measures?store=${this.store.code}`);
  }

  getListOfCountries() {
    return this.crudService.get(`/store/api/v1/country?store=${this.store.code}`);
  }

  getListOfZonesProvincesByCountry(countryCode) {
    const params = {
      'code': countryCode,
      'store': this.store.code
    };
    return this.crudService.get(`/store/api/v1/zones`, params);
  }
}
