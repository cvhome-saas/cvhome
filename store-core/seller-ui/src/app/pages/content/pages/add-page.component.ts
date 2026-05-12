import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from '../../shared/services/config.service';
import {NbToastrService} from '@nebular/theme';
import {validators} from '../../shared/validation/validators';
import {slugify} from '../../shared/utils/slugifying';

import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {ContentService} from "../services/content.service";
import {SelectedStoreService} from "../../shared/services/selected-store.service";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../store-management/services/store.service";

@Component({
  selector: 'add-page',
  standalone: false,
  templateUrl: './add-page.component.html',
  styleUrls: ['./add-page.component.scss'],
})
export class AddPageComponent implements OnInit {

  loader = false;
  uniqueCode: string;
  form: FormGroup;
  content: any;
  loadingList = false;
  action: string = 'save';
  languages = [];

  defaultLanguage: string;
  currentLanguage: string;

  isCodeExists = false;

  params: any;

  constructor(
    private contentService: ContentService,
    public router: Router,
    private toastr: NbToastrService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
    private fb: FormBuilder,
    private translate: TranslateService,
    private errorService: ErrorService,
    private selectedStoreService: SelectedStoreService,
    private storeService: StoreService
  ) {
    this.params = this.param();
  }

  get code() {
    return this.form.get('code');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  param() {
    return {
      store: '',
    };
  }

  ngOnInit() {
    this.loader = true;
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return zip(
          of(selectedStore),
          of(params),
          this.storeService.getStore(selectedStore),
          this.configService.getListOfSupportedLanguages(selectedStore),
          this.activatedRoute.queryParams
        )
      }))
      .subscribe({
        next: ([selectedStore, params, store, languages, queryParams]) => {
          this.params.store = selectedStore;
          this.uniqueCode = params.code
          this.languages = [...languages];
          this.addFormArray();
          this.defaultLanguage = store.defaultLanguage;
          this.selectLanguage(this.defaultLanguage);
          if (this.uniqueCode) {
            this.action = 'edit';
            this.getPage();
          } else {
            const suggestedCode = queryParams['code'];
            if (suggestedCode) {
              this.form.patchValue({
                code: suggestedCode,
              });
            }
          }
        },
        error: (err) => {
          this.loader = false;
        },
        complete: () => {
          this.loader = false;
        }
      });

    this.createForm();
  }

  addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          description: [''],
          title: [''],
          metaDescription: [''],
          name: ['', [Validators.required]],
          friendlyUrl: ['', [Validators.required]]
        })
      );
    });
  }

  fillFormArray() {
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.content != null && this.content.descriptions) {
        this.content.descriptions.forEach((description) => {
          if (desc.language === description.language) {
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              language: description.language,
              name: description.name,
              friendlyUrl: description.friendlyUrl,
              description: description.description,
              title: description.title,
              metaDescription: description.metaDescription,
            });
          }
        });
      }
    });
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
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
    }
    return invalid;
  }

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
    this.currentLanguage = lang;
    this.fillFormArray();
  }

  focusOutFunction(event) {
    const code = event.target.value.trim();
    this.contentService.checkCodePageExist(code)
      .subscribe(res => {
        this.isCodeExists = res.exists;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  urlTitle(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  save() {
    this.form.markAllAsTouched();
    if (this.findInvalidControls().length > 0) {
      return;
    }
    const object = this.form.value;

    //remove un necessary
    delete object.selectedLanguage;

    if (object.id) {

      this.contentService.updatePage(object.id, object)
        .subscribe({
          next: (data) => {
            this.loadingList = false;
            this.toastr.success('Page updated successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
    } else {
      this.contentService.createPage(object)
        .subscribe({
          next: (data) => {
            this.loadingList = false;
            this.toastr.success('Page added successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
    }

  }

  goToBack() {
    this.router.navigate(['/pages/content/pages/list']);
  }

  private getPage() {
    this.contentService.getPage(this.uniqueCode)
      .subscribe({
        next: (data) => {
          this.content = data;
          this.fillForm();
          this.loader = false;
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  private createForm() {
    this.form = this.fb.group({
      id: [''],
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      linkToMenu: [false],
      order: [0],
      selectedLanguage: [this.defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private fillForm() {
    this.form.patchValue({
      id: this.content.id,
      code: this.content.code,
      visible: this.content.visible,
      linkToMenu: this.content.linkToMenu,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();
    this.findInvalidControls();

  }
}
