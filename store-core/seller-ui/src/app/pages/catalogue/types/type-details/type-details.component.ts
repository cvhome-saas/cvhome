import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {validators} from '../../../shared/validation/validators';
import {TypesService} from '../services/types.service';
import {NbToastrService} from '@nebular/theme';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../../store-management/services/store.service";

@Component({
  selector: 'ngx-types',
  standalone: false,
  templateUrl: './type-details.component.html',
  styleUrls: ['./type-details.component.scss']
})
export class TypeDetailsComponent implements OnInit {

  form: FormGroup;
  loader: boolean = false;
  loaded: boolean = false;
  isReadonlyCode = false;
  isCodeExist = false;
  isValidCode = true;
  defaultLanguage: string;
  languages = [];
  type = {
    id: '',
    code: '',
    allowAddToCart: false,
    visible: '',
    description: {name: '', language: ''},
    descriptions: []
  }
  store: string;
  uniqueCode: string;

  constructor(
    private fb: FormBuilder,
    private translate: TranslateService,
    private router: Router,
    private errorService: ErrorService,
    private toastr: NbToastrService,
    private typesService: TypesService,
    private activatedRoute: ActivatedRoute,
    private configService: ConfigService,
    private selectedStoreService: SelectedStoreService,
    private storeService: StoreService
  ) {
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

  ngOnInit(): void {

    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return zip(
          of(selectedStore),
          of(params),
          this.storeService.getStore(selectedStore),
          this.configService.getListOfSupportedLanguages(selectedStore)
        )
      }))

      .subscribe({
        next: ([selectedStore, params, store, languages]) => {
          this.store = selectedStore;
          this.uniqueCode = params.id
          this.languages = [...languages];
          this.addFormArray();
          this.defaultLanguage = store.defaultLanguage;
          this.selectLanguage(this.defaultLanguage);
          if (this.uniqueCode) {
            this.fillData();
          }
          this.loaded = true;
        },
        error: (err) => {
          this.loaded = true;
          this.loader = false;
        },
        complete: () => {
          this.loaded = true;
          this.loader = false;
        }
      });
    this.createForm();

  }

  fillData() {
    this.typesService.getType(this.uniqueCode)
      .subscribe({
        next: (types) => {
          this.type.id = types.id;
          this.type.code = types.code;
          this.type.allowAddToCart = types.allowAddToCart;
          this.type.visible = types.visible;
          this.type.description = types.description;
          this.type.descriptions = types.descriptions;

          this.isReadonlyCode = true;
          if (this.type) {
            this.fillForm();
          }

        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })


  }

  addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          name: ['', [Validators.required]],
        })
      );
    });
  }

  fillFormArray() {
    this.form.value.descriptions.forEach((desc, index) => {
      if (desc.language === this.selectedLanguage.value) {
        this.type.descriptions.forEach((d, i) => {
          if (d.language === this.selectedLanguage.value) {
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              language: d.language,
              name: d.name,
            });
          }
        });
      }
    });
  }

  save() {
    this.loader = true;
    this.isValidCode = true;

    if (this.form.invalid) {
      if (this.code.invalid) {
        this.isValidCode = false;
      }

      this.loader = false;
      return;
    }

    let obj = this.form.value;

    if (this.type.id) {

      this.typesService.updateType(this.type.id, obj)
        .subscribe({
          next: (data) => {
            this.toastr.success(this.translate.instant('PRODUCT_TYPE.PRODUCT_TYPE_UPDATED'));
            this.loader = false;
          },
          error: (err) => {
            this.loader = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });

    } else {
      this.typesService.createType(obj)
        .subscribe({
          next: (data) => {
            this.toastr.success(this.translate.instant('PRODUCT_TYPE.PRODUCT_TYPE_CREATED'));
            this.goToBack();
            this.loader = false;

          },
          error: (err) => {
            this.loader = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          }
        });
    }
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/types/types-list']);
  }

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
  }

  checkCode(event) {
    const code = event.target.value.trim();
    this.isValidCode = true;
    this.typesService.checkCode(code)
      .subscribe(res => {
        this.isCodeExist = res.exists;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  private createForm() {
    this.form = this.fb.group({
      allowAddToCart: [true],
      visible: [false],
      code: [{value: '', disabled: false}, [Validators.required, Validators.pattern(validators.alphanumeric)]],
      selectedLanguage: this.defaultLanguage,
      descriptions: this.fb.array([]),
    });
  }

  private fillForm() {
    this.form.patchValue({
      allowAddToCart: this.type.allowAddToCart,
      visible: this.type.visible,
      code: this.type.code,
      selectedLanguage: this.defaultLanguage
    });
    if (this.type.id) {
      this.form.controls['code'].disable();
    }
    if (this.type.descriptions) {
      this.fillFormArray();
    }
  }

}
