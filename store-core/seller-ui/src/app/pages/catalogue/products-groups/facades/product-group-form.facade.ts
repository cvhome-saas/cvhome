import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {switchMap, zip} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ProductGroupFormService} from '../services/product-group-form.service';
import {ProductGroupsService} from 'seller-core/catalog';
import {ConfigService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {SelectedStoreService} from 'seller-core';
import {EMPTY_PAGE, PageT} from 'seller-core';
import {SupportedLanguageCode} from 'seller-core';
import {ProductGroupItem} from 'seller-core/catalog';
import {DatatablePageEvent} from 'seller-core';

@Injectable()
export class ProductGroupFormFacade {
  readonly formService = inject(ProductGroupFormService);
  private readonly productGroupsService = inject(ProductGroupsService);
  private readonly configService = inject(ConfigService);
  private readonly selectedStoreService = inject(SelectedStoreService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly loader = signal<boolean>(false);
  readonly isCodeUnique = signal<boolean>(true);
  readonly languages = signal<SupportedLanguageCode[]>([]);
  readonly store = signal<string>('');
  readonly uniqueCode = signal<string | undefined>(undefined);
  readonly rows = signal<ProductGroupItem[]>([]);
  readonly page = signal<PageT<never>>(EMPTY_PAGE);

  private defaultLanguage = '';
  private action: 'create' | 'edit' = 'create';
  private isLoaded = false;

  init(destroyRef: DestroyRef): void {
    zip([
      this.selectedStoreService.current(),
      this.activatedRoute.params,
      this.activatedRoute.queryParams
    ]).pipe(
      switchMap(([store, params, queryParams]) => {
        this.store.set(store);
        const code = params['code'];
        this.uniqueCode.set(code);
        if (!code) {
          const suggestedCode = queryParams['code'];
          if (suggestedCode) {
            this.formService.patchSuggestedCode(suggestedCode);
          }
        } else {
          this.action = 'edit';
        }
        return this.configService.getListOfSupportedLanguages(store);
      }),
      takeUntilDestroyed(destroyRef)
    ).subscribe({
      next: (languages) => {
        this.languages.set([...languages]);
        this.defaultLanguage = this.languages()[0]?.code;
        this.formService.initDescriptions(languages);
        this.formService.selectLanguage(this.defaultLanguage);
        if (this.action === 'edit') {
          this.loadGroup();
        }
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }

  private loadGroup(): void {
    this.loader.set(true);
    this.productGroupsService.getProductGroup(this.uniqueCode()).subscribe({
      next: (group) => {
        this.setRows(group.products || []);
        this.formService.patchForm(group, this.defaultLanguage);
        this.loader.set(false);
        this.isLoaded = true;
      },
      error: (err) => {
        this.loader.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  selectLanguage(langCode: string): void {
    this.formService.selectLanguage(langCode);
  }

  checkCode(code: string): void {
    if (!code || this.action === 'edit') return;
    this.productGroupsService.checkCode(code).subscribe({
      next: (res) => this.isCodeUnique.set(!res.exists),
      error: (err) => this.apiErrors.notify(err)
    });
  }

  save(): void {
    this.formService.form.markAllAsTouched();
    if (this.formService.form.invalid || !this.isCodeUnique()) {
      return;
    }
    this.loader.set(true);
    this.productGroupsService.createProductGroup(this.formService.prepareSaveData()).subscribe({
      next: () => {
        this.loader.set(false);
        this.goToBack();
      },
      error: (err) => {
        this.loader.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/catalogue/products-groups/groups-list']);
  }

  onItemSelect(item: ProductGroupItem): void {
    const groupCode = this.uniqueCode();
    this.loader.set(true);
    this.productGroupsService.addProductToGroup(item.id, groupCode).subscribe({
      next: () => {
        this.loader.set(false);
        this.setRows([...this.rows(), item]);
        this.notify.success('PRODUCT_GROUP.PRODUCT_ADDED');
      },
      error: (err) => {
        this.loader.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  onItemDeSelect(item: ProductGroupItem): void {
    const groupCode = this.uniqueCode();
    this.loader.set(true);
    this.productGroupsService.removeProductFromGroup(item.id, groupCode).subscribe({
      next: () => {
        this.loader.set(false);
        this.setRows(this.rows().filter((it) => it.id !== item.id));
        this.notify.success('PRODUCT_GROUP.PRODUCT_REMOVED');
      },
      error: (err) => {
        this.loader.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  onPageChange(pageInfo: DatatablePageEvent): void {
    if (!this.uniqueCode() || !this.isLoaded) return;
    this.page.update((current) => ({...current, pageNumber: pageInfo.offset}));
    this.loadGroup();
  }

  private setRows(rows: ProductGroupItem[]): void {
    this.rows.set(rows);
    this.page.update((current) => ({
      ...current,
      totalPages: 1,
      totalElements: rows.length,
      size: rows.length
    }));
  }
}
