import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from 'seller-core';
import {SupportedLanguageCode} from 'seller-core';
import {CategoryDescription, ReadableCategory} from 'seller-core/catalog';

@Injectable()
export class CategoryFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    parent: ['root', [Validators.required]],
    store: [''],
    visible: [false],
    code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
    sortOrder: [0, [Validators.required, Validators.pattern(validators.number)]],
    selectedLanguage: ['', [Validators.required]],
    descriptions: this.fb.array([])
  });

  get code() {
    return this.form.get('code');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return this.form.get('descriptions') as FormArray;
  }

  initDescriptions(languages: SupportedLanguageCode[]): void {
    this.descriptions.clear();
    languages.forEach((lang) => {
      this.descriptions.push(this.createDescriptionFormGroup(lang.code));
    });
  }

  createDescriptionFormGroup(langCode: string): FormGroup {
    return this.fb.group({
      language: [langCode, [Validators.required]],
      name: ['', [Validators.required]],
      highlights: [''],
      friendlyUrl: ['', [Validators.required]],
      description: [''],
      title: [''],
      metaDescription: ['']
    });
  }

  patchEmptyForm(storeId: string, defaultLanguage: string): void {
    this.form.patchValue({
      store: storeId,
      sortOrder: 0,
      selectedLanguage: defaultLanguage
    });
  }

  patchForm(category: ReadableCategory, languages: SupportedLanguageCode[], defaultLanguage: string): void {
    if (!category) return;

    this.form.patchValue({
      parent: category.parent === null || !category.parent ? 'root' : category.parent.code,
      store: category.store,
      visible: category.visible,
      code: category.code,
      sortOrder: category.sortOrder,
      selectedLanguage: defaultLanguage
    });

    this.descriptions.clear();
    languages.forEach((lang) => {
      const desc: CategoryDescription = category.descriptions?.find((d) => d.language === lang.code) || {};
      this.descriptions.push(this.fb.group({
        language: [lang.code, [Validators.required]],
        name: [desc.name || '', [Validators.required]],
        highlights: [desc.highlights || ''],
        friendlyUrl: [desc.friendlyUrl || '', [Validators.required]],
        description: [desc.description || ''],
        title: [desc.title || ''],
        metaDescription: [desc.metaDescription || '']
      }));
    });
  }
}
