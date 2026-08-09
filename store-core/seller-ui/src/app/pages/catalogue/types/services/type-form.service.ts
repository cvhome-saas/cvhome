import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from 'seller-core';
import {SupportedLanguageCode} from 'seller-core';
import {ProductTypeDescription, ReadableProductType} from 'seller-core/catalog';

@Injectable()
export class TypeFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    allowAddToCart: [true],
    visible: [false],
    code: [{value: '', disabled: false}, [Validators.required, Validators.pattern(validators.alphanumeric)]],
    selectedLanguage: [''],
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
      this.descriptions.push(this.fb.group({
        language: [lang.code, [Validators.required]],
        name: ['', [Validators.required]]
      }));
    });
  }

  selectLanguage(langCode: string): void {
    this.form.patchValue({selectedLanguage: langCode});
  }

  patchForm(type: ReadableProductType, defaultLanguage: string): void {
    this.form.patchValue({
      allowAddToCart: type.allowAddToCart,
      visible: type.visible,
      code: type.code,
      selectedLanguage: defaultLanguage
    });

    if (type.id) {
      this.form.controls['code'].disable();
    }

    (type.descriptions || []).forEach((desc: ProductTypeDescription) => {
      const index = this.descriptions.controls.findIndex(
        (control) => control.value.language === desc.language
      );
      if (index !== -1) {
        this.descriptions.at(index).patchValue({
          language: desc.language,
          name: desc.name
        });
      }
    });
  }
}
