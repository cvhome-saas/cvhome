import {Injectable, inject, signal} from '@angular/core';
import {FormArray, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from '../../../shared/services/error.service';
import {ContentService} from '../../services/content.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {AddPageFormService} from '../services/add-page.form.service';
import {mergeMap, of, zip} from 'rxjs';
import {SupportedLanguageCode} from '../../../shared/services/config.service';
import {HttpParamsLike} from '../../../shared/services/crud.service';
import {ReadableContentPage} from '../../models/content.model';

@Injectable()
export class AddPageFacade {
  private readonly formService = inject(AddPageFormService);
  private readonly contentService = inject(ContentService);
  private readonly router = inject(Router);
  private readonly configService = inject(ConfigService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);

  readonly loader = signal<boolean>(false);
  readonly loadingList = signal<boolean>(false);
  readonly action = signal<string>('save');
  readonly languages = signal<SupportedLanguageCode[]>([]);
  readonly defaultLanguage = signal<string>('');
  readonly currentLanguage = signal<string>('');
  readonly uniqueCode = signal<string>('');
  readonly isCodeExists = signal<boolean>(false);
  readonly content = signal<ReadableContentPage>(null);

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
            this.getPage();
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
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  selectLanguage(lang: string): void {
    this.form.patchValue({ selectedLanguage: lang });
    this.currentLanguage.set(lang);
    this.formService.fillFormArray(this.form, this.content());
  }

  focusOutFunction(event: Event): void {
    const code = (event.target as HTMLInputElement).value.trim();
    this.contentService.checkCodePageExist(code)
      .subscribe({
        next: (res) => this.isCodeExists.set(res.exists),
        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
      });
  }

  urlTitle(event: string, index: number): void {
    this.formService.urlTitle(this.form, event, index);
  }

  save(): void {
    this.form.markAllAsTouched();
    if (this.formService.findInvalidControls(this.form).length > 0) {
      return;
    }
    const object = { ...this.form.value };
    delete object.selectedLanguage;

    if (object.id) {
      this.contentService.updatePage(object.id, object)
        .subscribe({
          next: () => {
            this.loadingList.set(false);
            this.errorService.success('Page updated successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList.set(false);
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          }
        });
    } else {
      this.contentService.createPage(object)
        .subscribe({
          next: () => {
            this.loadingList.set(false);
            this.errorService.success('Page added successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList.set(false);
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          }
        });
    }
  }

  goToBack(): void {
    this.router.navigate(['/pages/content/pages/list']);
  }

  private getPage(): void {
    this.contentService.getPage(this.uniqueCode())
      .subscribe({
        next: (data) => {
          this.content.set(data);
          this.formService.fillForm(this.form, data, this.defaultLanguage());
          this.loader.set(false);
        },
        error: (err) => {
          this.loader.set(false);
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }
}
