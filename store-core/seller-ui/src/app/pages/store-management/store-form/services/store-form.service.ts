import {Injectable, inject} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';

@Injectable()
export class StoreFormService {
  private readonly fb = inject(FormBuilder);

  createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      pod: this.fb.group({
        id: [''],
      }),
      phone: ['', [Validators.required]],
      theme: ['', [Validators.required]],
      colorTheme: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.pattern(validators.emailPattern)]],
      address: this.fb.group({
        searchControl: [''],
        stateProvince: [{value: '', disabled: false}],
        country: ['', [Validators.required]],
        address: ['', [Validators.required]],
        postalCode: ['', [Validators.required]],
        city: ['', [Validators.required]]
      }),
      supportedLanguages: [[], [Validators.required]],
      defaultLanguage: ['', [Validators.required]],
      currency: ['', [Validators.required]],
      currencyFormatNational: [true],
      weight: ['', [Validators.required]],
      dimension: ['', [Validators.required]],
      requireLoginForOrderPlacement: [true],
      inBusinessSince: [new Date()],
    }, {validators: defaultLanguageMustBeInSupportedValidator});
  }

  fillForm(form: FormGroup, store: any): string[] {
    const supportedLanguagesSelected: string[] = [];
    if (store.supportedLanguages) {
      store.supportedLanguages.forEach((lang: string) => {
        supportedLanguagesSelected.push(lang);
      });
    }

    form.patchValue({
      id: store.id,
      name: store.name,
      theme: store.theme,
      colorTheme: store.colorTheme,
      phone: store.phone,
      email: store.email,
      supportedLanguages: store.supportedLanguages,
      defaultLanguage: store.defaultLanguage,
      currency: store.currency,
      currencyFormatNational: store.currencyFormatNational,
      weight: store.weight,
      dimension: store.dimension,
      requireLoginForOrderPlacement: store.requireLoginForOrderPlacement,
      inBusinessSince: store.inBusinessSince ? new Date(store.inBusinessSince) : new Date(),
    });

    if (store.pod) {
      form.controls['pod'].patchValue({id: store.pod.id});
    }

    if (store.address) {
      form.controls['address'].patchValue({
        searchControl: '',
        stateProvince: store.address.stateProvince,
        country: store.address.country,
        address: store.address.address,
        postalCode: store.address.postalCode,
        city: store.address.city
      });
    }

    form.get("pod")?.get("id")?.disable();

    return supportedLanguagesSelected;
  }
}

export function defaultLanguageMustBeInSupportedValidator(group: FormGroup): Record<string, boolean> | null {
  const defaultLangControl: AbstractControl = group.get('defaultLanguage');
  const supportedLangsControl: AbstractControl = group.get('supportedLanguages');

  if (!defaultLangControl || !supportedLangsControl) {
    return null;
  }

  const defaultLanguageValue = defaultLangControl.value;
  const supportedLanguagesValue = supportedLangsControl.value;

  if (defaultLanguageValue && supportedLanguagesValue && Array.isArray(supportedLanguagesValue) && supportedLanguagesValue.length > 0) {
    if (!supportedLanguagesValue.includes(defaultLanguageValue)) {
      return {'defaultLanguageNotInSupported': true};
    }
  }

  return null;
}
