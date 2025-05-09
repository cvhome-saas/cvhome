import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';

import {BrandService} from '../services/brand.service';
import {ConfigService} from '../../../../shared/services/config.service';
import {TranslateService} from '@ngx-translate/core';
import {Router} from '@angular/router';
import {validators} from '../../../../shared/validation/validators';
import {slugify} from '../../../../shared/utils/slugifying';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";
import {Store} from "../../../store-management/models/store";

@Component({
  selector: 'ngx-brand-form',
  standalone: false,
  templateUrl: './brand-form.component.html',
  styleUrls: ['./brand-form.component.scss']
})
export class BrandFormComponent implements AfterViewInit, OnInit {
  @Input() brand;
  @Input() title;
  @Input() store: Store;
  form: FormGroup;
  loader = false;
  defaultLanguage: string;
  languages = [];
  isCodeUnique = true;

  constructor(
    private brandService: BrandService,
    private fb: FormBuilder,
    private configService: ConfigService,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private errorService: ErrorService
  ) {
  }

  ngOnInit(): void {
    this.createForm();
  }


  get code() {
    return this.form.get('code');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  ngAfterViewInit(): void {
    this.defaultLanguage = this.store.defaultLanguage;
    this.createForm();
    this.loader = true;
    this.configService.getListOfSupportedLanguages(this.store.id)
      .subscribe(res => {
        this.languages = [...res];
        this.createForm();
        this.addFormArray();
        if (this.brand.id) {
          this.fillForm();
        }
        this.loader = false;
      }, err => {
        this.loader = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }


  createForm() {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      order: ['', [Validators.required, Validators.pattern(validators.number)]],
      selectedLanguage: [this.defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
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
      code: this.brand.code,
      order: this.brand.order,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();
  }

  fillFormArray() {
    //console.log(this.brand.descriptions);
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.brand != null && this.brand.descriptions) {
        this.brand.descriptions.forEach((description) => {
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
    //this.fillFormArray();
  }

  changeName(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  checkCode(event) {
    const code = event.target.value;
    this.brandService.checkBrandCode(this.store, code)
      .subscribe(res => {
        this.isCodeUnique = !(res.exists && (this.brand.code !== code));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  save() {
    const brandObject = this.form.value;
    // save important values for filling empty field in result object
    const tmpObj = {
      name: '',
      friendlyUrl: ''
    };
    brandObject.descriptions.forEach((el) => {
      if (tmpObj.name === '' && el.name !== '') {
        tmpObj.name = el.name;
      }
      if (tmpObj.friendlyUrl === '' && el.friendlyUrl !== '') {
        tmpObj.friendlyUrl = el.friendlyUrl;
      }
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (!tmpObj.hasOwnProperty(elKey) && el[elKey] !== '') {
            tmpObj[elKey] = el[elKey];
          }
        }
      }
    });

    // check required fields
    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || brandObject.code === '') {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
    } else {
      brandObject.descriptions.forEach((el) => {
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
      brandObject.descriptions.forEach(el => {
        for (const elKey in el) {
          if (el.hasOwnProperty(elKey)) {
            el.name = el.name.trim(); // trim name
            if (typeof el[elKey] === 'undefined') {
              el[elKey] = '';
            }
          }
        }
      });

      if (!this.isCodeUnique) {
        this.toastr.danger(this.translate.instant('COMMON.CODE_EXISTS'));
        return;
      }

      if (this.brand.id) {
        this.brandService.updateBrand(this.store, this.brand.id, brandObject)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('BRAND.BRAND_UPDATED'));
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      } else {
        this.brandService.createBrand(this.store, brandObject)
          .subscribe(result => {
            this.toastr.success(this.translate.instant('BRAND.BRAND_CREATED'));
            this.router.navigate(['pages/catalogue/brands/brands-list']);
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    }
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/brands/brands-list']);
  }
}
