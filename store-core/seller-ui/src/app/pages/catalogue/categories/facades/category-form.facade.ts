import {Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {FormArray, FormGroup} from '@angular/forms';
import {CategoryFormService} from '../services/category-form.service';
import {CategoryService} from '../services/category.service';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from '../../../shared/services/error.service';
import {slugify} from '../../../shared/utils/slugifying';
import {Store} from '../../../store-management/models/store';
import {zip} from 'rxjs';

@Injectable()
export class CategoryFormFacade {
  readonly formService = inject(CategoryFormService);
  private readonly categoryService = inject(CategoryService);
  private readonly configService = inject(ConfigService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly loading = signal<boolean>(false);
  readonly isCodeUnique = signal<boolean>(true);
  readonly roots = signal<any[]>([]);
  readonly languages = signal<any[]>([]);
  readonly defaultLanguage = signal<string>('');

  private categoryData: any;
  private storeData: Store;

  init(category: any, store: Store): void {
    this.categoryData = category || {};
    this.storeData = store;
    if (!store) return;

    this.defaultLanguage.set(store.defaultLanguage || 'en');
    this.loader.set(true);

    const params = {store: store.id};

    zip([
      this.categoryService.getListOfCategories(params),
      this.configService.getListOfSupportedLanguages(store.id)
    ]).subscribe({
      next: ([categoriesRes, languagesRes]) => {
        this.loader.set(false);

        const content = categoriesRes.content ? [...categoriesRes.content] : [];
        content.push({id: 0, code: 'root', children: []});

        const rootList: any[] = [];
        content.forEach((el) => {
          this.getChildren(el, rootList);
        });

        rootList.sort((a, b) => {
          if (a.code < b.code) return -1;
          if (a.code > b.code) return 1;
          return 0;
        });

        this.roots.set(rootList);
        this.languages.set([...languagesRes]);

        this.formService.initDescriptions(languagesRes);
        this.formService.patchEmptyForm(store.id, this.defaultLanguage());

        if (this.categoryData.id) {
          this.formService.patchForm(this.categoryData, languagesRes, this.defaultLanguage());
        }
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private getChildren(node: any, rootList: any[]): void {
    if (node.id === this.categoryData?.id) return;
    if (node.children && node.children.length !== 0) {
      rootList.push(node);
      node.children.forEach((el: any) => {
        this.getChildren(el, rootList);
      });
    } else {
      rootList.push(node);
    }
  }

  selectLanguage(langCode: string): void {
    this.formService.form.patchValue({
      selectedLanguage: langCode
    });
  }

  changeName(event: any, index: number): void {
    this.formService.descriptions.at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  checkCode(event: any): void {
    const code = event.target.value;
    if (!code) return;

    this.categoryService.checkCategoryCode(code).subscribe({
      next: (res) => {
        this.isCodeUnique.set(!(res.exists && (this.categoryData.code !== code)));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  save(): void {
    this.loading.set(true);
    const categoryObject = this.prepareSaveData();

    const tmpObj: any = {
      name: '',
      friendlyUrl: ''
    };

    categoryObject.descriptions.forEach((el: any) => {
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

    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || categoryObject.code === '') {
      this.errorService.error('COMMON.FILL_REQUIRED_FIELDS', null);
      this.loading.set(false);
      return;
    }

    categoryObject.descriptions.forEach((el: any) => {
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (el[elKey] === '' && tmpObj[elKey] !== '') {
            el[elKey] = tmpObj[elKey];
          }
        }
      }
    });

    categoryObject.descriptions.forEach((el: any) => {
      for (const elKey in el) {
        if (el.hasOwnProperty(elKey)) {
          if (el.name) {
            el.name = el.name.trim();
          }
          if (typeof el[elKey] === 'undefined') {
            el[elKey] = '';
          }
        }
      }
    });

    if (!this.isCodeUnique()) {
      this.errorService.error('COMMON.CODE_EXISTS', null);
      this.loading.set(false);
      return;
    }

    const invalidControls = this.findInvalidControls();
    if (invalidControls.length > 0) {
      this.errorService.error('COMMON.FILL_REQUIRED_FIELDS', null);
      this.loading.set(false);
      return;
    }

    if (this.categoryData.id) {
      this.categoryService.updateCategory(this.categoryData.id, categoryObject).subscribe({
        next: () => {
          this.loading.set(false);
          this.errorService.success('CATEGORY_FORM.CATEGORY_UPDATED');
        },
        error: (err) => {
          this.loading.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    } else {
      this.categoryService.addCategory(categoryObject).subscribe({
        next: () => {
          this.loading.set(false);
          this.errorService.success('CATEGORY_FORM.CATEGORY_CREATED');
          this.router.navigate(['pages/catalogue/categories/categories-list']);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    }
  }

  goToBack(): void {
    this.router.navigate(['pages/catalogue/categories/categories-list']);
  }

  private prepareSaveData(): any {
    const data = {...this.formService.form.value};
    const matchedCategory = this.roots().find((el) => el.code === data.parent);
    if (matchedCategory) {
      data.parent = {id: matchedCategory.id, code: matchedCategory.code};
    } else {
      data.parent = {id: 0, code: 'root'};
    }
    return data;
  }

  private findInvalidControls(): string[] {
    const invalidControls: string[] = [];
    const recursiveFunc = (form: FormGroup | FormArray) => {
      Object.keys(form.controls).forEach((field) => {
        const control = form.get(field);
        if (control?.invalid) invalidControls.push(field);
        if (control instanceof FormGroup || control instanceof FormArray) {
          recursiveFunc(control);
        }
      });
    };
    recursiveFunc(this.formService.form);
    return invalidControls;
  }
}
