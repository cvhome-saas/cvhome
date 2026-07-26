import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';

@Injectable()
export class AddBoxFormService {
  private readonly fb = inject(FormBuilder);

  createForm(defaultLanguage: string): FormGroup {
    return this.fb.group({
      id: 0,
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      selectedLanguage: [defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  addFormArray(form: FormGroup, languages: any[]): void {
    const control = form.controls['descriptions'] as FormArray;
    languages.forEach(lang => {
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

  fillForm(form: FormGroup, content: any, defaultLanguage: string): void {
    form.patchValue({
      id: content.id,
      code: content.code,
      visible: content.visible,
      selectedLanguage: defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray(form, content);
  }

  fillFormArray(form: FormGroup, content: any): void {
    const descriptions = form.value.descriptions || [];
    descriptions.forEach((desc: any, index: number) => {
      if (content != null && content.descriptions) {
        content.descriptions.forEach((description: any) => {
          if (desc.language === description.language) {
            (form.get('descriptions') as FormArray).at(index).patchValue({
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

  findInvalidControls(form: FormGroup): string[] {
    const invalid: string[] = [];
    const controls = form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    return invalid;
  }
}
