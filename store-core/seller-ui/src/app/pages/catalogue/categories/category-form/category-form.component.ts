import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {CategoryService} from '../services/category.service';
import {ConfigService} from '../../../../shared/services/config.service';
import {TranslateService} from '@ngx-translate/core';
import {validators} from '../../../../shared/validation/validators';
import {slugify} from '../../../../shared/utils/slugifying';
import {ErrorService} from "../../../../shared/services/error.service";
import {Store} from "../../../store-management/models/store";
import {zip} from "rxjs";


@Component({
  selector: 'ngx-category-form',
  standalone: false,
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.scss']
})
export class CategoryFormComponent implements AfterViewInit, OnInit {
  @Input() category: any;
  @Input() store: Store;
  form: FormGroup;
  roots = [];
  languages = [];
  defaultLanguage: string;

  //loader image
  loader = false;
  loading = false;

  //category code must be unique
  isCodeUnique = true;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private configService: ConfigService,
    private errorService: ErrorService,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService,
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

  get titles(): FormArray {
    return <FormArray>this.form.get('titles');
  }

  get names(): FormArray {
    return <FormArray>this.form.get('names');
  }

  ngAfterViewInit(): void {
    const params = {
      store: this.store.id
    };
    this.defaultLanguage = this.store.defaultLanguage
    zip([this.categoryService.getListOfCategories(params), this.configService.getListOfSupportedLanguages(this.store.id)])
      .subscribe({
        next: ([categories, languages]) => {
          this.loader = false;

          categories.content.push({id: 0, code: 'root', children: []});
          categories.content.forEach((el) => {
            this.getChildren(el);
          });
          this.roots.sort((a, b) => {
            if (a.code < b.code)
              return -1;
            if (a.code > b.code)
              return 1;
            return 0;
          });

          this.languages = [...languages];
          this.addFormArray();
          this.fillEmptyForm();
          if (this.category.id) {
            this.fillForm();
          }

        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);

        },
        complete: () => {
          this.loader = false;

        }
      })

  }

  getChildren(node) {
    if (node.id === this.category.id) return;
    if (node.children && node.children.length !== 0) {
      this.roots.push(node);
      node.children.forEach((el) => {
        this.getChildren(el);
      });
    } else {
      this.roots.push(node);
    }
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
          metaDescription: [''],
        })
      );
    });
  }

  fillEmptyForm() {
    this.form.patchValue({
      store: this.store.id,
      sortOrder: 0,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });

  }

  fillForm() {
    this.form.patchValue({
      parent: this.category.parent === null ? 'root' : this.category.parent.code,
      store: this.category.store,
      visible: this.category.visible,
      code: this.category.code,
      sortOrder: this.category.sortOrder,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();
  }

  fillFormArray() {
    //each supported language
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.category != null && this.category.descriptions) {
        this.category.descriptions.forEach((description) => {
          if (desc.language === description.language) {
            //6 fields + language
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              language: description.language,
              name: description.name,
              highlights: description.highlights,
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

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
  }

  changeName(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });

  }

  checkCode(event) {
    const code = event.target.value;
    this.categoryService.checkCategoryCode(code)
      .subscribe(res => {
        this.isCodeUnique = !(res.exists && (this.category.code !== code));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  save() {
    this.loading = true;
    const categoryObject = this.prepareSaveData();

    /**
     * TODO put in utility
     */
    const tmpObj = {
      name: '',
      friendlyUrl: ''
    };
    categoryObject.descriptions.forEach((el) => {
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
    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || categoryObject.code === '') {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
    } else {
      categoryObject.descriptions.forEach((el) => {
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
      categoryObject.descriptions.forEach(el => {
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
        this.loading = false;
        return;
      }

      let errors = this.findInvalidControls();
      if (errors.length > 0) {
        this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
        this.loading = false;
        return;
      }
      if (this.category.id) {
        this.categoryService.updateCategory(this.category.id, categoryObject)
          .subscribe(result => {
            this.loading = false;
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_UPDATED'));
          }, err => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      } else {
        this.categoryService.addCategory(categoryObject)
          .subscribe(result => {
            this.loading = false;
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_CREATED'));
            this.router.navigate(['pages/catalogue/categories/categories-list']);
          }, err => {
            this.loading = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          });
      }
    }
  }

  public findInvalidControls(): string[] {
    var invalidControls: string[] = [];
    let recursiveFunc = (form: FormGroup | FormArray) => {
      Object.keys(form.controls).forEach(field => {
        const control = form.get(field);
        if (control.invalid) invalidControls.push(field);
        if (control instanceof FormGroup) {
          recursiveFunc(<FormGroup>control);
        } else if (control instanceof FormArray) {
          recursiveFunc(<FormArray>control);
        }
      });
    }
    recursiveFunc(this.form);
    return invalidControls;
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/categories/categories-list']);
  }

  private createForm() {
    this.form = this.fb.group({
      parent: ['root', [Validators.required]],
      store: [this.store.id],
      visible: [false],
      code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
      sortOrder: [0, [Validators.required, Validators.pattern(validators.number)]],
      selectedLanguage: ['', [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private prepareSaveData() {
    const data = this.form.value;
    const category = this.roots.find((el) => el.code === data.parent);
    data.parent = {id: category.id, code: category.code};
    return data;
  }

}
