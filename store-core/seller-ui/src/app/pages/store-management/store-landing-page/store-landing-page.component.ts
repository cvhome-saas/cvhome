import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {forkJoin} from 'rxjs';
import {ErrorService} from "../../../shared/services/error.service";


@Component({
  selector: 'ngx-store-landing-page',
  standalone: false,
  templateUrl: './store-landing-page.component.html',
  styleUrls: ['./store-landing-page.component.scss']
})
export class StoreLandingPageComponent implements OnInit {
  form: FormGroup;
  store;
  languages = [];
  selectedItem = '1';
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
    }, {
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
  loader = false;
  page;

  constructor(
    private fb: FormBuilder,
    private storeService: StoreService,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  ngOnInit() {
    this.createForm();
    const store = this.activatedRoute.snapshot.paramMap.get('code');

    forkJoin([this.storeService.getPageContent(store,'LANDING_PAGE'), this.storeService.getStore(store)])
      .subscribe(([res, st]) => {
        if (!res.status) {//404 should not rais an error
          this.page = res;
        }
        this.store = st;
        this.languages = this.store.supportedLanguages;
        this.createForm();
        this.fillForm();
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }

  addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          name: ['', [Validators.required]],
          metaDescription: [''],
          id: '',
          keyWords: [''],
          description: [''],
        })
      );
    });
  }

  fillForm() {
    this.form.patchValue({
      descriptions: [],
      selectedLanguage: 'en',
    });
    if (this.page) {
      this.fillFormArray();
    }
  }

  fillFormArray() {
    this.form.patchValue({
      id: this.page.id
    });
    this.form.value.descriptions.forEach((desc, index) => {
      this.page.descriptions.forEach((description) => {
        if (desc.language === description.language) {
          (<FormArray>this.form.get('descriptions')).at(index).patchValue({
            language: description.language,
            name: description.name,
            metaDescription: description.metaDescription,
            keyWords: description.keyWords,
            description: description.description,
          });
        }
      });
    });
  }

  save() {
    this.form.patchValue({name: this.store.name});
    this.form.patchValue({code: 'LANDING_PAGE'});
    console.log(JSON.stringify(this.form.value));
    if (this.page && this.page.id) {
      this.storeService.updatePageContent(this.store.id, this.page.id, this.form.value)
        .subscribe(res => {
          this.toastr.success(this.translate.instant('STORE_LANDING.PAGE_UPDATED'));
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    } else {
      JSON.stringify(this.form.value);
      this.storeService.createPageContent(this.form.value, this.store.id)
        .subscribe(res => {
          this.toastr.success(this.translate.instant('STORE_LANDING.PAGE_ADDED'));
          this.router.navigate(['pages/store-management/store/', this.store.id]);
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    }
  }

  private createForm() {
    this.form = this.fb.group({
      selectedLanguage: ['en', [Validators.required]],
      code: [''],
      id: '',
      descriptions: this.fb.array([]),
    });
    this.addFormArray();
  }

}
