import {Injectable, inject} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../../shared/validation/validators';

@Injectable()
export class ProductFormService {
  private readonly fb = inject(FormBuilder);

  readonly form: FormGroup = this.fb.group({
    sku: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
    visible: [false],
    dateAvailable: [new Date()],
    manufacturer: ['', [Validators.required]],
    type: [''],
    display: [true],
    canBePurchased: [true],
    timeBound: [false],
    price: ['', [Validators.required]],
    quantity: ['', [Validators.required, Validators.pattern(validators.number)]],
    sortOrder: ['', [Validators.required, Validators.pattern(validators.number)]],
    productSpecifications: this.fb.group({
      weight: [''],
      height: [''],
      width: [''],
      length: ['']
    }),
    selectedLanguage: ['', [Validators.required]],
    descriptions: this.fb.array([])
  });

  get sku() {
    return this.form.get('sku');
  }

  get manufacturer() {
    return this.form.get('manufacturer');
  }

  get price() {
    return this.form.get('price');
  }

  get quantity() {
    return this.form.get('quantity');
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
        name: ['', [Validators.required]],
        highlights: [''],
        friendlyUrl: ['', [Validators.required]],
        description: [''],
        title: [''],
        keyWords: [''],
        metaDescription: ['']
      }));
    });
  }

  selectLanguage(langCode: string): void {
    this.form.patchValue({selectedLanguage: langCode});
  }

  patchForm(product: any, defaultLanguage: string): void {
    this.form.patchValue({
      sku: product.sku,
      visible: product.visible,
      canBePurchased: product.canBePurchased,
      dateAvailable: new Date(product.dateAvailable),
      manufacturer: product.manufacturer == null ? '' : product.manufacturer.code,
      type: product.type == null ? '' : product.type.code,
      price: this.toPrice(product.inventory.price),
      quantity: product.inventory.quantity,
      productSpecifications: {
        weight: product.productSpecifications.weight,
        height: product.productSpecifications.height,
        width: product.productSpecifications.width,
        length: product.productSpecifications.length
      },
      sortOrder: product.sortOrder,
      selectedLanguage: defaultLanguage
    });

    (product.descriptions || []).forEach((description: any) => {
      const index = this.descriptions.controls.findIndex(
        (control) => control.value.language === description.language
      );
      if (index !== -1) {
        this.descriptions.at(index).patchValue({
          language: description.language,
          name: description.name,
          highlights: description.highlights,
          friendlyUrl: description.friendlyUrl,
          description: description.description,
          title: description.title,
          keyWords: description.keyWords,
          metaDescription: description.metaDescription
        });
      }
    });
  }

  private toPrice(price: string): number {
    try {
      return parseFloat(price.replace(/,/g, ''));
    } catch {
      return 0;
    }
  }
}
