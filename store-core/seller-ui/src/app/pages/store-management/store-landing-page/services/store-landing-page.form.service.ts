import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {LandingPageContent} from 'seller-core/stores';

@Injectable()
export class StoreLandingPageFormService {
  private readonly fb = inject(FormBuilder);

  createForm(): FormGroup {
    return this.fb.group({
      selectedLanguage: ['en', [Validators.required]],
      code: [''],
      id: '',
      descriptions: this.fb.array([]),
    });
  }

  addFormArray(form: FormGroup, languages: string[]): void {
    const control = form.controls['descriptions'] as FormArray;
    languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang, [Validators.required]],
          name: ['', [Validators.required]],
          metaDescription: [''],
          id: '',
          keyWords: [''],
          description: [''],
        })
      );
    });
  }

  fillForm(form: FormGroup, page: LandingPageContent): void {
    form.patchValue({
      selectedLanguage: 'en',
    });
    if (page) {
      form.patchValue({
        id: page.id
      });
      const descriptions = form.value.descriptions || [];
      descriptions.forEach((desc: {language?: string}, index: number) => {
        if (page.descriptions) {
          page.descriptions.forEach((description) => {
            if (desc.language === description.language) {
              (form.get('descriptions') as FormArray).at(index).patchValue({
                language: description.language,
                name: description.name,
                metaDescription: description.metaDescription,
                keyWords: description.keyWords,
                description: description.description,
              });
            }
          });
        }
      });
    }
  }
}
