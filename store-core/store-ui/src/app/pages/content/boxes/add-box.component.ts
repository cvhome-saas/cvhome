import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {validators} from '../../../shared/validation/validators';
import {TranslateService} from '@ngx-translate/core';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../services/content.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../store-management/services/store.service";

@Component({
  selector: 'add-box',
  standalone: false,
  templateUrl: './add-box.component.html',
  styleUrls: ['./add-box.component.scss'],
})
export class AddBoxComponent implements OnInit {
  loader = false;
  form: FormGroup;
  content: any;
  languages = [];
  uniqueCode: string;
  isCodeExists = false;
  action: any = 'save'

  defaultLanguage: string;
  currentLanguage: string;
  description: Array<any> = []
  params: any;

  constructor(
    private fb: FormBuilder,
    private contentService: ContentService,
    public router: Router,
    private toastr: NbToastrService,
    private configService: ConfigService,
    private activatedRoute: ActivatedRoute,
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
            this.loadContent();
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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.loader = false;
        }
      });
    this.createForm();
  }

  public findInvalidControls() {
    const invalid = [];
    const controls = this.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    return invalid;
  }

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
    this.currentLanguage = lang;
  }

  goToBack() {
    this.router.navigate(['/pages/content/boxes/list']);
  }

  private loadContent() {
    const box = this.contentService.getBox(this.uniqueCode, this.params)
      .subscribe(data => {
        this.content = data;
        this.fillForm();
        this.loader = false;
      }, err => {
        this.loader = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  private createForm() {
    this.form = this.fb.group({
      id: 0,
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      selectedLanguage: [this.defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          description: [''],
          name: [''],
          title: [''],
          id: 0
        })
      );
    });
  }

  private fillForm() {
    this.form.patchValue({
      id: this.content.id,
      code: this.content.code,
      visible: this.content.visible,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();
    this.findInvalidControls();

  }

  private fillFormArray() {
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.content != null && this.content.descriptions) {
        this.content.descriptions.forEach((description) => {
          if (desc.language === description.language) {
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              id: description.id,
              language: description.language,
              description: description.description,
              name: description.name,
              title: description.title
            });
          }
        });
      }
    });
  }

  checkCode(event) {
    //check if box code already exists
    const code = event.target.value.trim();
    this.contentService.checkCodeBoxExist(this.params.store, code, this.param())
      .subscribe(res => {
        this.isCodeExists = res.exists;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });

  }

  save() {
    this.form.markAllAsTouched();
    if (this.findInvalidControls().length > 0) {
      return;
    }
    this.loader = true;

    //manouver resulting object
    var object = this.form.value;

    //remove un necessary
    delete object.selectedLanguage;


    /**
     * TODO revise put in utility
     */
    const tmpObj = {
      name: '',
      friendlyUrl: ''
    };
    object.descriptions.forEach((el) => {
      if (el.name === '') {
        el.name = object.code;
      }
    });

    // check required fields
    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || object.code === '') {

    } else {
      object.descriptions.forEach((el) => {
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
      object.descriptions.forEach(el => {
        for (const elKey in el) {
          if (el.hasOwnProperty(elKey)) {
            el.name = el.name.trim(); // trim name
            if (typeof el[elKey] === 'undefined') {
              el[elKey] = '';
            }
          }
        }
      });
    }

    /**
     let errors = this.findInvalidControls();
     if (errors.length > 0) {
     this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
     return;
     }
     **/
    //for debugging
    //console.log(JSON.stringify(categoryObject));
    //return;

    //console.log('Content saved ' + JSON.stringify(object));

    if (object.id > 0) {//update
      //set content name required field
      this.contentService.updateBox(this.content.id, this.params.store, object, this.param())
        .subscribe(data => {
          this.loader = false;
          this.toastr.success(this.translate.instant('CONTENT.CONTENT_UPDATED'));
          this.router.navigate(['/pages/content/boxes/list']);
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loader = false;
        });

    } else {
      this.contentService.createBox(this.params.store, object)
        .subscribe(data => {
          this.loader = false;
          this.toastr.success(this.translate.instant('PRODUCT.PRODUCT_UPDATED'));
          this.router.navigate(['/pages/content/boxes/list']);
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loader = false;
        });

    }
    this.loader = false;
  }
}
