import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';

import {ConfigService} from '../../../shared/services/config.service';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {validators} from '../../../shared/validation/validators';
import {forkJoin} from 'rxjs';
import {ErrorService} from "../../../shared/services/error.service";
import {Pods, PodService} from "../services/pod.service";

@Component({
  selector: 'ngx-store-form',
  standalone: false,
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.scss']
})
export class StoreFormComponent implements OnInit {
  @Input() title: string;
  @Input() store: any;
  @Input() isCancel: string;

  supportedLanguagesList = [];
  supportedLanguagesSelected = [];
  supportedCurrency = [];
  supportedTheme = [];
  supportedColorThemes = [];
  weightList = [];
  sizeList = [];
  countries = [];
  pods: Pods = [];
  form: FormGroup;
  loader = false;
  isReadonlyName = false;
  isReadonlyPodSelection = false;
  defaultPodName = 'DEFAULT'
  isNameUnique = true;
  selectedItem = '5';
  submitted = false;
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
    private fb: FormBuilder,
    private configService: ConfigService,
    private storeService: StoreService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private errorService: ErrorService,
    private podService: PodService) {
  }

  get name() {
    return this.form.get('name');
  }

  get pod() {
    return this.form.get('pod').get('id');
  }

  get phone() {
    return this.form.get('phone');
  }

  get theme() {
    return this.form.get('theme');
  }

  get colorTheme() {
    return this.form.get('colorTheme');
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

  get supportedLanguages() {
    return this.form.get('supportedLanguages');
  }

  get defaultLanguage() {
    return this.form.get('defaultLanguage');
  }

  get currency() {
    return this.form.get('currency');
  }

  get weight() {
    return this.form.get('weight');
  }

  get dimension() {
    return this.form.get('dimension');
  }

  ngOnInit() {
    this.loader = true;
    forkJoin([
      this.configService.getListOfCountries(),
      this.configService.getListOfSupportedCurrency(),
      this.configService.getWeightAndSizes(),
      this.storeService.getSupportedThemes(),
      this.storeService.getSupportedColorThemes(),
      this.podService.listPods()
    ])
      .subscribe(([countries, currencies, measures, themes, colorThemes, pods]) => {
        this.countries = [...countries];
        this.supportedCurrency = [...currencies];
        this.supportedTheme = [...themes];
        this.supportedColorThemes = [...colorThemes];
        this.weightList = [...measures.weights];
        this.sizeList = [...measures.measures];
        this.pods = pods;
        this.supportedLanguagesList = this.configService.getListOfGlobalLanguages();
        this.loader = false;
      }, err => {
        this.loader = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });


    this.createForm();
  }

  fillForm() {

    this.store.supportedLanguages.forEach(lang => {
      this.supportedLanguagesSelected.push(lang);
    });

    this.form.patchValue({
      id: this.store.id,
      name: this.store.name,
      theme: this.store.theme,
      colorTheme: this.store.colorTheme,
      phone: this.store.phone,
      email: this.store.email,
      supportedLanguages: this.store.supportedLanguages,
      defaultLanguage: this.store.defaultLanguage,
      currency: this.store.currency,
      currencyFormatNational: this.store.currencyFormatNational,
      weight: this.store.weight,
      dimension: this.store.dimension,
      inBusinessSince: new Date(this.store.inBusinessSince),
    });

    this.form.controls['pod'].patchValue({id: this.store.pod.id});

    this.form.controls['address'].patchValue({searchControl: ''});
    this.form.controls['address'].patchValue({stateProvince: this.store.address.stateProvince}, {disabled: false});
    this.form.controls['address'].patchValue({country: this.store.address.country});
    this.form.controls['address'].patchValue({address: this.store.address.address});
    this.form.controls['address'].patchValue({postalCode: this.store.address.postalCode});
    this.form.controls['address'].patchValue({city: this.store.address.city});
    this.isReadonlyName = true;
    this.isReadonlyPodSelection = true;
    this.form.get("pod").get("id").disable()
    this.cdr.markForCheck();

  }

  save() {
    this.submitted = true;
    if (this.form.valid) {
      this.form.controls['address'].patchValue({country: this.form.value.address.country});
      this.form.controls['address'].patchValue({stateProvince: this.form.value.address.stateProvince});
      const storeObj = this.form.value;
      storeObj.supportedLanguages = this.supportedLanguagesSelected;
      if (this.store && this.store.id) {
        storeObj.id = this.store.id;
        this.storeService.updateStore(storeObj)
          .subscribe(store => {
            this.toastr.success(this.translate.instant('STORE_FORM.STORE_UPDATED'));
            this.router.navigate(['pages/store-management/stores-list']);
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
                }, err => {
                  this.errorService.error('ERROR.SYSTEM_ERROR', err);
                });
            }
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
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
    return this.store.supportedLanguages.find((l: any) => l === language.code) != undefined;
  }

  checkName(event) {
    this.storeService.checkIfStoreExist(this.form.value.name)
      .subscribe(res => {
        this.isNameUnique = !res.exists
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }

  goToBack() {
    this.router.navigate(['pages/store-management/stores-list']);
  }

  private createForm() {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      pod: this.fb.group({
        id: [''],
      }),
      phone: ['', [Validators.required]],
      theme: ['', [Validators.required]],
      colorTheme: ['', [Validators.required]],
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
      currency: ['', [Validators.required]],
      currencyFormatNational: [true],
      weight: ['', [Validators.required]],
      dimension: ['', [Validators.required]],
      inBusinessSince: [new Date()],
    }, {validators: defaultLanguageMustBeInSupportedValidator});

    if (this.store && this.store.id) {
      this.fillForm();
    }

  }

}

export function defaultLanguageMustBeInSupportedValidator(group: FormGroup): { [key: string]: boolean } | null {
  const defaultLangControl: AbstractControl = group.get('defaultLanguage');
  const supportedLangsControl: AbstractControl = group.get('supportedLanguages');

  if (!defaultLangControl || !supportedLangsControl) {
    return null; // Controls not found, should not happen in a well-defined form
  }

  const defaultLanguageValue = defaultLangControl.value;
  const supportedLanguagesValue = supportedLangsControl.value; // This should be an array of language codes

  // Only proceed if both controls have values and supportedLanguages is a non-empty array.
  // The 'required' validator on individual controls will handle cases where they are empty.
  if (defaultLanguageValue && supportedLanguagesValue && Array.isArray(supportedLanguagesValue) && supportedLanguagesValue.length > 0) {
    if (!supportedLanguagesValue.includes(defaultLanguageValue)) {
      console.log("rasing error")
      return {'defaultLanguageNotInSupported': true}; // Validation error for the group
    }
  }

  return null; // No error
}
