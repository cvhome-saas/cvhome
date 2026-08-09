import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {TableStateService} from 'seller-core';
import {OrgService} from 'seller-core/orgs';
import {ApiErrorService} from 'seller-core';
import {StorePageRequest, PageT} from 'seller-core';
import {DatatablePageEvent} from 'seller-core';
import {Org} from 'seller-core/orgs';

@Injectable()
export class OrgListFacade {
  readonly tableState = inject(TableStateService<Org, StorePageRequest>);
  private readonly orgService = inject(OrgService);
  private readonly router = inject(Router);
  private readonly apiErrors = inject(ApiErrorService);

  init(): void {
    this.loadOrgs();
  }

  loadOrgs(): void {
    this.tableState.setLoading(true);
    this.orgService.getListOfOrg(this.tableState.params()).subscribe({
      next: (data) => {
        const mapped: PageT<Org> = {
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
    this.loadOrgs();
  }

  onEdit(row: Org): void {
    this.router.navigate(['pages/org-management/org/', row.id.id]);
  }
}
