import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {TableStateService} from '../../../shared/table/table-state.service';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class OrgListFacade {
  readonly tableState = inject(TableStateService);
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
        this.tableState.setPage(data);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onPageChange(event: any): void {
    this.tableState.setParams({
      ...this.tableState.params(),
      page: event.offset
    });
    this.loadOrgs();
  }

  onEdit(row: any): void {
    this.router.navigate(['pages/org-management/org/', row.id.id]);
  }
}
