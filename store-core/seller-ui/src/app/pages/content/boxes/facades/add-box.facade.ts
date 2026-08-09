import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {ContentService} from 'seller-core/content';
import {SelectedStoreService} from 'seller-core';
import {StoreService} from 'seller-core/stores';
import {AddBoxFormService} from '../services/add-box.form.service';
import {mergeMap, of, zip} from 'rxjs';
import {SupportedLanguageCode} from 'seller-core';
import {HttpParamsLike} from 'seller-core';
import {ContentDescription, ReadableContentBox} from 'seller-core/content';

@Injectable()
export class AddBoxFacade {
  private readonly formService = inject(AddBoxFormService);
  private readonly contentService = inject(ContentService);
  private readonly router = inject(Router);
  private readonly configService = inject(ConfigService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);

  readonly loader = signal<boolean>(false);
  readonly action = signal<'save' | 'edit'>('save');
  readonly languages = signal<SupportedLanguageCode[]>([]);
  readonly defaultLanguage = signal<string>('');
  readonly currentLanguage = signal<string>('');
  readonly uniqueCode = signal<string>('');
  readonly isCodeExists = signal<boolean>(false);
  readonly content = signal<ReadableContentBox>(null);

  params: HttpParamsLike = { store: '' };
  form!: FormGroup;

  get code() {
    return this.form ? this.form.get('code') : null;
  }

  get descriptions(): FormArray {
    return this.form ? (this.form.get('descriptions') as FormArray) : null!;
  }

  get selectedLanguage() {
    return this.form ? this.form.get('selectedLanguage') : null;
  }

  init(): void {
    this.loader.set(true);
    zip([this.selectedStoreService.current(), this.activatedRoute.params])
      .pipe(mergeMap(([selectedStore, params]) => {
        return zip(
          of(selectedStore),
          of(params),
          this.storeService.getStore(selectedStore),
          this.configService.getListOfSupportedLanguages(selectedStore),
          this.activatedRoute.queryParams
        );
      }))
      .subscribe({
        next: ([selectedStore, params, store, languages, queryParams]) => {
          this.params.store = selectedStore;
          this.uniqueCode.set(params.code);
          this.languages.set([...languages]);
          this.defaultLanguage.set(store.defaultLanguage);
          this.form = this.formService.createForm(store.defaultLanguage);
          this.formService.addFormArray(this.form, languages);
          this.selectLanguage(store.defaultLanguage);

          if (params.code) {
            this.action.set('edit');
            this.loadContent();
          } else {
            const suggestedCode = queryParams['code'];
            if (suggestedCode) {
              this.form.patchValue({ code: suggestedCode });
            }
            this.loader.set(false);
          }
        },
        error: (err) => {
          this.loader.set(false);
          this.apiErrors.notify(err);
        }
      });
  }

  selectLanguage(lang: string): void {
    this.form.patchValue({ selectedLanguage: lang });
    this.currentLanguage.set(lang);
  }

  checkCode(event: Event): void {
    const code = (event.target as HTMLInputElement).value.trim();
    this.contentService.checkCodeBoxExist(code, this.params)
      .subscribe({
        next: (res) => this.isCodeExists.set(res.exists),
        error: (err) => this.apiErrors.notify(err)
      });
  }

  goToBack(): void {
    this.router.navigate(['/pages/content/boxes/list']);
  }

  save(): void {
    this.form.markAllAsTouched();
    if (this.formService.findInvalidControls(this.form).length > 0) {
      return;
    }
    this.loader.set(true);

    const object = { ...this.form.value };
    delete object.selectedLanguage;

    const tmpObj = { name: '', friendlyUrl: '' };
    object.descriptions.forEach((el: ContentDescription) => {
      if (el.name === '') {
        el.name = object.code;
      }
    });

    if (tmpObj.name === '' || tmpObj.friendlyUrl === '' || object.code === '') {
      // noop
    } else {
      object.descriptions.forEach((el: ContentDescription) => {
        for (const elKey in el) {
          if (Object.prototype.hasOwnProperty.call(el, elKey)) {
            if (el[elKey] === '' && tmpObj[elKey as keyof typeof tmpObj] !== '') {
              el[elKey] = tmpObj[elKey as keyof typeof tmpObj];
            }
          }
        }
      });
      object.descriptions.forEach((el: ContentDescription) => {
        for (const elKey in el) {
          if (Object.prototype.hasOwnProperty.call(el, elKey)) {
            if (el.name) {
              el.name = el.name.trim();
            }
            if (typeof el[elKey] === 'undefined') {
              el[elKey] = '';
            }
          }
        }
      });
    }

    if (object.id > 0) {
      const contentVal = this.content();
      this.contentService.updateBox(contentVal ? contentVal.id : object.id, object, this.params)
        .subscribe({
          next: () => {
            this.loader.set(false);
            this.notify.success('CONTENT.CONTENT_UPDATED');
            this.router.navigate(['/pages/content/boxes/list']);
          },
          error: (err) => {
            this.apiErrors.notify(err);
            this.loader.set(false);
          }
        });
    } else {
      this.contentService.createBox(object)
        .subscribe({
          next: () => {
            this.loader.set(false);
            this.notify.success('PRODUCT.PRODUCT_UPDATED');
            this.router.navigate(['/pages/content/boxes/list']);
          },
          error: (err) => {
            this.apiErrors.notify(err);
            this.loader.set(false);
          }
        });
    }
  }

  private loadContent(): void {
    this.contentService.getBox(this.uniqueCode(), this.params)
      .subscribe({
        next: (data) => {
          this.content.set(data);
          this.formService.fillForm(this.form, data, this.defaultLanguage());
          this.loader.set(false);
        },
        error: (err) => {
          this.loader.set(false);
          this.apiErrors.notify(err);
        }
      });
  }
}
