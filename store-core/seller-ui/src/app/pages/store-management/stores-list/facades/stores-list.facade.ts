import {Injectable, inject} from '@angular/core';
import {Router} from '@angular/router';
import {NbDialogService} from '@nebular/theme';
import {ShowcaseDialogComponent} from "../../../shared/components/showcase-dialog/showcase-dialog.component";
import {ErrorService} from "../../../shared/services/error.service";
import {StoreService} from "../../services/store.service";
import {TableStateService} from "../../../shared/table/table-state.service";
import {PageT, StorePageRequest} from "../../../shared/table/table.types";
import {PageEvent} from "@swimlane/ngx-datatable";
import {ManagerStore} from "../../../shared/models/commons";

@Injectable()
export class StoresListFacade {
  private readonly storeService = inject(StoreService);
  private readonly router = inject(Router);
  private readonly dialogService = inject(NbDialogService);
  private readonly errorService = inject(ErrorService);
  public readonly tableState = inject(TableStateService<ManagerStore, StorePageRequest>);

  init(): void {
    this.loadStores();
  }

  onPageChange(event: PageEvent): void {
    this.tableState.patchParams({ page: event.offset });
    this.loadStores();
  }

  loadStores(): void {
    this.tableState.setLoading(true);
    const params = this.tableState.params();
    this.storeService.getListOfStores(params).subscribe({
      next: (page) => {
        const mapped: PageT<ManagerStore> = {
          content: page.content,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          pageNumber: page.number
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

  onEdit(row: ManagerStore): void {
    this.router.navigate(['pages/store-management/store/', row.id.id]);
  }

  onDelete(row: ManagerStore): void {
    this.dialogService.open(ShowcaseDialogComponent, {
      context: {
        title: 'Are you sure!',
        text: 'Do you really want to remove this entity?'
      }
    }).onClose.subscribe(res => {
      if (res) {
        this.storeService.deleteStore(row.id.id).subscribe({
          next: () => {
            this.errorService.success('USER_FORM.USER_REMOVED');
            this.loadStores();
          },
          error: (err) => {
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          }
        });
      }
    });
  }
}
