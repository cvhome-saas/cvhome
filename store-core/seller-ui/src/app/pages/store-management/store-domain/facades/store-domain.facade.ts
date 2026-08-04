import {Injectable, inject, signal} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../../services/store.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {StoreDomainFormService} from '../services/store-domain.form.service';
import {StoreDomainComponentValidatorContext} from '../store-domain.validator';
import {DatatablePageEvent} from '../../../shared/table/table-events';
import {EMPTY_PAGE, PageT} from '../../../shared/table/table.types';
import {zip} from 'rxjs';
import {sideMenuLinks} from '../../services/constents';
import {ManagerStoreDomain} from '../../models/store';

@Injectable()
export class StoreDomainFacade implements StoreDomainComponentValidatorContext {
  private readonly formService = inject(StoreDomainFormService);
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  readonly isSubmited = signal<boolean>(false);
  readonly store = signal<string>('');
  readonly perPageSize = 50;
  readonly loading = signal<boolean>(false);
  readonly page = signal<PageT<never>>(EMPTY_PAGE);
  readonly rows = signal<ManagerStoreDomain[]>([]);
  readonly selectedItem = signal<string>('2');
  readonly sideMenuLinks = sideMenuLinks;

  saasProperties = {alis: '', domain: ''};
  shortenPodId = '';

  form: FormGroup = this.formService.createForm(this);

  get domain() {
    return this.form.get('domain');
  }

  init(): void {
    const storeCode = this.activatedRoute.snapshot.paramMap.get('code');
    if (storeCode) {
      this.store.set(storeCode);
      this.getAllocations();
    }
  }

  route(link: string): void {
    this.router.navigate(['pages/store-management/' + link + '/', this.store()]);
  }

  setPage(pageInfo: DatatablePageEvent): void {
    const pg = { ...this.page() };
    pg.pageNumber = pageInfo.offset;
    this.page.set(pg);
    this.getAllocations();
  }

  getAllocations(): void {
    const storeCode = this.store();
    if (!storeCode) return;

    this.loading.set(true);
    zip(
      this.storeService.saasProperties(),
      this.storeService.getAllocations(storeCode),
      this.storeService.storePodByStoreId(storeCode)
    ).subscribe({
      next: (it) => {
        this.loading.set(false);
        this.saasProperties = it[0];
        this.shortenPodId = it[2].shortenPodId;
        if (it[1] && it[1].length > 0) {
          this.rows.set(it[1]);
          this.page.set({
            ...EMPTY_PAGE,
            totalPages: 1,
            totalElements: it[1].length,
            size: it[1].length
          });
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      },
      complete: () => {
        this.loading.set(false);
      }
    });
  }

  onAccess(row: ManagerStoreDomain): void {
    window.open(`https://${this.generateDomain(row)}`, '_blank');
  }

  onDelete(domainStr: string): void {
    this.loading.set(true);
    this.storeService.removeDomain(this.store(), domainStr).subscribe({
      next: () => {
        this.loading.set(false);
        this.notify.success('STORE.DOMAIN_REMOVED');
        this.getAllocations();
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      }
    });
  }

  createDomain(domainStr: string): void {
    this.loading.set(true);
    this.storeService.allocateDomain(this.store(), domainStr).subscribe({
      next: () => {
        this.loading.set(false);
        this.notify.success('STORE.DOMAIN_CREATED');
        this.getAllocations();
      },
      error: (err) => {
        this.loading.set(false);
        this.apiErrors.notify(err);
      },
      complete: () => {
        this.form.reset();
        this.isSubmited.set(false);
      }
    });
  }

  save(): void {
    this.isSubmited.set(true);
    this.form.markAllAsTouched();
    if (!this.form.valid) {
      return;
    }
    const object = this.form.value;
    this.createDomain(object.domain);
  }

  public generateDomain(row: ManagerStoreDomain): string {
    if (row.domainType === 'SUB_DOMAIN') {
      return row.domain + '.' + this.podServerDomain();
    } else {
      return row.domain;
    }
  }

  public podServerDomain(): string {
    return this.saasProperties.alis + '-' + this.shortenPodId + '.' + this.saasProperties.domain;
  }
}
