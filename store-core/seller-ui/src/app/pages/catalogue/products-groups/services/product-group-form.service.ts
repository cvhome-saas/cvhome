import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';

@Injectable()
export class ProductGroupFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
    active: [true],
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

  initDescriptions(languages: any[]): void {
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

  patchSuggestedCode(code: string): void {
    this.form.patchValue({code});
  }

  patchForm(group: any, defaultLanguage: string): void {
    this.form.patchValue({
      code: group.code,
      active: group.active,
      selectedLanguage: defaultLanguage
    });

    (group.descriptions || []).forEach((desc: any) => {
      const index = this.descriptions.controls.findIndex(
        (control) => control.value.language === desc.language
      );
      if (index !== -1) {
        this.descriptions.at(index).patchValue({name: desc.name});
      }
    });
  }

  descriptionNameStatus(index: number): string {
    const name = this.descriptions.at(index)?.get('name');
    if (!name || (!name.dirty && !name.touched)) return 'basic';
    return name.invalid ? 'danger' : 'success';
  }

  languageTabStatus(langCode: string): string {
    const ctrl = this.descriptions.controls.find((c) => c.get('language')?.value === langCode);
    if (ctrl && ctrl.get('name')?.invalid && ctrl.get('name')?.touched) return 'danger';
    return this.selectedLanguage.value === langCode ? 'primary' : 'basic';
  }

  prepareSaveData(): any {
    const {code, active, descriptions} = this.form.value;
    return {code, active, descriptions};
  }
}
