import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {TableStateService} from '../../../shared/table/table-state.service';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';
import {StorePageRequest, PageT} from '../../../shared/table/table.types';
import {DatatablePageEvent} from '../../../shared/table/table-events';
import {Org} from '../../model/org';

@Injectable()
export class OrgListFacade {
  readonly tableState = inject(TableStateService<Org, StorePageRequest>);
  private readonly orgService = inject(OrgService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

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
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
