import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';
import {slugify} from '../../../shared/utils/slugifying';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class AddPageFormService {
  private readonly fb = inject(FormBuilder);
  private readonly errorService = inject(ErrorService);

  createForm(defaultLanguage: string): FormGroup {
    return this.fb.group({
      id: [''],
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      linkToMenu: [false],
      order: [0],
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
          title: [''],
          metaDescription: [''],
          name: ['', [Validators.required]],
          friendlyUrl: ['', [Validators.required]]
        })
      );
    });
  }

  fillForm(form: FormGroup, content: any, defaultLanguage: string): void {
    form.patchValue({
      id: content.id,
      code: content.code,
      visible: content.visible,
      linkToMenu: content.linkToMenu,
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
              language: description.language,
              name: description.name,
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

  urlTitle(form: FormGroup, event: any, index: number): void {
    (form.get('descriptions') as FormArray).at(index).patchValue({
      friendlyUrl: slugify(event)
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
    if (invalid.length > 0) {
      this.errorService.error('COMMON.FILL_REQUIRED_FIELDS', null);
    }
    return invalid;
  }
}
