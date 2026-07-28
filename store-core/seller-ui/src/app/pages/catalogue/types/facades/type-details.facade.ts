import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NbToastrService} from '@nebular/theme';
import {TranslateService} from '@ngx-translate/core';
import {TypeFormService} from '../services/type-form.service';
import {TypesService} from '../services/types.service';
import {ConfigService} from '../../../shared/services/config.service';
import {ErrorService} from '../../../shared/services/error.service';
import {SelectedStoreService} from '../../../shared/services/selected-store.service';
import {StoreService} from '../../../store-management/services/store.service';
import {zip} from 'rxjs';

@Injectable()
export class TypeDetailsFacade {
  readonly formService = inject(TypeFormService);
  private readonly typesService = inject(TypesService);
  private readonly configService = inject(ConfigService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly toastr = inject(NbToastrService);
  private readonly translate = inject(TranslateService);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);
  readonly loaded = signal<boolean>(false);
  readonly isCodeExist = signal<boolean>(false);
  readonly isValidCode = signal<boolean>(true);
  readonly isReadonlyCode = signal<boolean>(false);
  readonly languages = signal<any[]>([]);

  private typeData: any = null;
  private defaultLanguage = '';

  init(): void {
    zip([this.selectedStoreService.current(), this.activatedRoute.params]).subscribe({
      next: ([selectedStore, params]) => {
        const typeId = params.id;
        this.loadFormContext(selectedStore, typeId);
      },
      error: (err) => {
        this.loaded.set(true);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private loadFormContext(selectedStore: string, typeId: string): void {
    zip([
      this.storeService.getStore(selectedStore),
      this.configService.getListOfSupportedLanguages(selectedStore)
    ]).subscribe({
      next: ([store, languages]) => {
        this.languages.set([...languages]);
        this.defaultLanguage = store.defaultLanguage;
        this.formService.initDescriptions(languages);
        this.formService.selectLanguage(this.defaultLanguage);

        if (typeId) {
          this.loadType(typeId);
        } else {
          this.loaded.set(true);
        }
      },
      error: (err) => {
        this.loaded.set(true);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  private loadType(typeId: string): void {
    this.typesService.getType(typeId).subscribe({
      next: (type) => {
        this.typeData = type;
        this.formService.patchForm(type, this.defaultLanguage);
        this.isReadonlyCode.set(true);
        this.loaded.set(true);
      },
      error: (err) => {
        this.loaded.set(true);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  selectLanguage(langCode: string): void {
    this.formService.selectLanguage(langCode);
  }

  checkCode(code: string): void {
    this.isValidCode.set(true);
    this.typesService.checkCode(code).subscribe({
      next: (res) => this.isCodeExist.set(res.exists),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }

  save(): void {
    this.loader.set(true);
    this.isValidCode.set(true);

    if (this.formService.form.invalid) {
      if (this.formService.code.invalid) {
        this.isValidCode.set(false);
      }
      this.loader.set(false);
      return;
    }

    const typeObject = this.formService.form.value;

    const request$ = this.typeData?.id
      ? this.typesService.updateType(this.typeData.id, typeObject)
      : this.typesService.createType(typeObject);

    request$.subscribe({
      next: () => {
        this.loader.set(false);
        this.toastr.success(this.translate.instant(
          this.typeData?.id ? 'PRODUCT_TYPE.PRODUCT_TYPE_UPDATED' : 'PRODUCT_TYPE.PRODUCT_TYPE_CREATED'
        ));
        if (!this.typeData?.id) {
          this.goToBack();
        }
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/catalogue/types/types-list']);
  }
}
