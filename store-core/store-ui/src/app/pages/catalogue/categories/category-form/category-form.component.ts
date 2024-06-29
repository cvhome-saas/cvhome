import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {CategoryService} from '../services/category.service';
import {ConfigService} from '../../../shared/services/config.service';
import {TranslateService} from '@ngx-translate/core';
import {StorageService} from '../../../shared/services/storage.service';
import {validators} from '../../../shared/validation/validators';
import {slugify} from '../../../shared/utils/slugifying';
import {ImageBrowserComponent} from "../../../shared/components/image-browser/image-browser.component";

declare var $: any;

@Component({
  selector: 'ngx-category-form',
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.scss']
})
export class CategoryFormComponent implements OnInit {
  @Input() category: any;
  form: FormGroup;
  roots = [];
  perPage = 100;
  languages = [];
  defaultLanguage :string;

  //loader image
  loader = false;
  loading = false;

  //category code must be unique
  isCodeUnique = true;

  params = this.loadParams();

  loadParams() {
    return {
      lang: this.storageService.getLanguage(),
      store: "",
      count: this.perPage,
      page: 0
    };
  }

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private configService: ConfigService,
    private storageService: StorageService,
    private router: Router,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private dialogService: NbDialogService
  ) {
    this.defaultLanguage = this.translate.defaultLang;
  }

  ngOnInit() {
    this.loader = true;
  }

  init() {
    this.categoryService.getListOfCategories(this.params)
      .subscribe(res => {
        res.categories.push({id: 0, code: 'root', children: []});
        res.categories.forEach((el) => {
          this.getChildren(el);
        });
        this.roots.sort((a, b) => {
          if (a.code < b.code)
            return -1;
          if (a.code > b.code)
            return 1;
          return 0;
        });
        //this.roots = [...res.categories];
        //console.log(JSON.stringify(this.roots));
      });

    //determines how many languages should be supported
    this.configService.getListOfSupportedLanguages(this.params.store)
      .subscribe(res => {
        this.languages = [...res];
        this.createForm();
        this.addFormArray();
        this.fillEmptyForm();
        if (this.category.id) {
          this.fillForm();
        }
        this.loader = false;
      });
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

  private createForm() {
    this.form = this.fb.group({
      parent: ['root', [Validators.required]],
      store: [this.params.store],
      visible: [false],
      code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
      sortOrder: [0, [Validators.required, Validators.pattern(validators.number)]],
      selectedLanguage: ['', [Validators.required]],
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
          metaDescription: [''],
        })
      );
    });
  }

  fillEmptyForm() {
    this.form.patchValue({
      store: this.params.store,
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
    this.categoryService.checkCategoryCode(code, this.params.store)
      .subscribe(res => {
        this.isCodeUnique = !(res.exists && (this.category.code !== code));
      });
  }

  private prepareSaveData() {
    const data = this.form.value;
    const category = this.roots.find((el) => el.code === data.parent);
    data.parent = {id: category.id, code: category.code};
    return data;
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
        this.categoryService.updateCategory(this.category.id, categoryObject, this.params.store)
          .subscribe(result => {
            this.loading = false;
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_UPDATED'));
          });
      } else {
        this.categoryService.addCategory(categoryObject, this.params.store)
          .subscribe(result => {
            this.loading = false;
            this.toastr.success(this.translate.instant('CATEGORY_FORM.CATEGORY_CREATED'));
            this.router.navigate(['pages/catalogue/categories/categories-list']);
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

  customButton(context) {
    const me = this;
    const ui = $.summernote.ui;
    const button = ui.button({
      contents: '<i class="note-icon-picture"></i>',
      tooltip: 'Gallery',
      container: '.note-editor',
      className: 'note-btn',
      click: function () {
        me.dialogService.open(ImageBrowserComponent, {
          context: {
            store: me.params.store
          }
        }).onClose.subscribe(name => name && context.invoke('editor.pasteHTML', '<img src="' + name + '">'));
      }
    });
    return button.render();
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/categories/categories-list']);
  }

  onSelectStore(e) {
    this.params.store = e.id
    this.init()
  }

}
