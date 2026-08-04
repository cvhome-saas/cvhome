import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {Pod, PodService} from '../../store-management/services/pod.service';
import {ErrorService} from '../../shared/services/error.service';
import {TableStateService} from '../../shared/table/table-state.service';
import {PageRequest} from '../../shared/table/table.types';
import {DatatablePageEvent} from '../../shared/table/table-events';

@Injectable()
export class PodListFacade {
  private readonly podService = inject(PodService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);
  readonly tableState = inject(TableStateService<Pod, PageRequest>);

  init(): void {
    this.loadPage();
  }

  loadPage(): void {
    this.tableState.setLoading(true);
    this.podService.findAllPods(this.tableState.params()).subscribe({
      next: (page) => {
        this.tableState.setPage(page);
        this.tableState.setLoading(false);
      },
      error: (err) => {
        this.tableState.setLoading(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  onPageChange(event: DatatablePageEvent): void {
    this.tableState.patchParams({page: event.offset});
    this.loadPage();
  }

  onEdit(row: Pod): void {
    this.router.navigate(['pages/pod-management/pod/', row.id.id]);
  }

  onDelete(row: Pod): void {
    this.podService.delete(row.id.id).subscribe({
      next: () => this.loadPage(),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
