import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';

@Injectable()
export class BrandFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
    order: [0, [Validators.required, Validators.pattern(validators.number)]],
    selectedLanguage: ['', [Validators.required]],
    descriptions: this.fb.array([])
  });

  get code() {
    return this.form.get('code');
  }

  get order() {
    return this.form.get('order');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return this.form.get('descriptions') as FormArray;
  }

  initDescriptions(languages: any[]): void {
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
      keyWords: [''],
      metaDescription: ['']
    });
  }

  patchForm(brand: any, languages: any[], defaultLang: string): void {
    if (!brand) return;

    this.form.patchValue({
      code: brand.code || '',
      order: brand.order || 0,
      selectedLanguage: defaultLang
    });

    this.descriptions.clear();
    languages.forEach((lang) => {
      const desc = brand.descriptions?.find((d: any) => d.language === lang.code) || {};
      this.descriptions.push(this.fb.group({
        language: [lang.code, [Validators.required]],
        name: [desc.name || '', [Validators.required]],
        highlights: [desc.highlights || ''],
        friendlyUrl: [desc.friendlyUrl || '', [Validators.required]],
        description: [desc.description || ''],
        title: [desc.title || ''],
        keyWords: [desc.keyWords || ''],
        metaDescription: [desc.metaDescription || '']
      }));
    });
  }
}
