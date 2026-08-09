import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map, mergeMap} from 'rxjs/operators';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TableStateService} from 'seller-core';
import {OrgService} from 'seller-core/orgs';
import {ApiErrorService} from 'seller-core';
import {Org} from 'seller-core/orgs';
import {ORG_SIDEMENU_LINKS} from '../../constants/org-management.constants';
import {PageT, StorePageRequest} from 'seller-core';
import {DatatablePageEvent} from 'seller-core';
import {ManagerStore} from 'seller-core';

@Injectable()
export class OrgStoresListFacade {
  readonly tableState = inject(TableStateService<ManagerStore, {id: string} & Partial<StorePageRequest>>);
  private readonly orgService = inject(OrgService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly apiErrors = inject(ApiErrorService);

  readonly org = signal<Org | null>(null);
  readonly selectedItem = signal<string>('2');
  readonly sidemenuLinks = ORG_SIDEMENU_LINKS;

  init(destroyRef: DestroyRef): void {
    this.activatedRoute.params.pipe(
      map(params => params['id']),
      mergeMap(id => this.orgService.getOrg(id)),
      takeUntilDestroyed(destroyRef)
    ).subscribe({
      next: (orgData) => {
        this.org.set(orgData);
        this.loadStores();
      },
      error: (err) => {
        this.apiErrors.notify(err);
      }
    });
  }

  loadStores(): void {
    const currentOrg = this.org();
    if (!currentOrg) return;

    this.tableState.setLoading(true);
    const params = {
      ...this.tableState.params(),
      id: currentOrg.id.id
    };

    this.orgService.getOrgStoresList(params).subscribe({
      next: (data) => {
        const mapped: PageT<ManagerStore> = {
          content: data.content,
          size: data.size,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          pageNumber: data.number
        };
        this.tableState.setPage(mapped);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.apiErrors.notify(err);
      }
    });
  }

  onPageChange(event: DatatablePageEvent): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadStores();
  }

  route(link: string): void {
    const currentOrg = this.org();
    if (currentOrg) {
      this.router.navigate([link.replace('{OrgId}', currentOrg.id.id)]);
    }
  }
}
