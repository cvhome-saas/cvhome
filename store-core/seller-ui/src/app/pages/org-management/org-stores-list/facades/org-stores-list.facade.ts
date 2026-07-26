import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {map, mergeMap} from 'rxjs/operators';
import {TableStateService} from '../../../shared/table/table-state.service';
import {OrgService} from '../../services/org.service';
import {ErrorService} from '../../../shared/services/error.service';
import {Org} from '../../model/org';
import {ORG_SIDEMENU_LINKS} from '../../constants/org-management.constants';

@Injectable()
export class OrgStoresListFacade {
  readonly tableState = inject(TableStateService);
  private readonly orgService = inject(OrgService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly org = signal<Org | null>(null);
  readonly selectedItem = signal<string>('2');
  readonly sidemenuLinks = ORG_SIDEMENU_LINKS;

  init(): void {
    this.activatedRoute.params.pipe(
      map(params => params['id']),
      mergeMap(id => this.orgService.getOrg(id))
    ).subscribe({
      next: (orgData) => {
        this.org.set(orgData);
        this.loadStores();
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
    this.loadStores();
  }

  route(link: string): void {
    const currentOrg = this.org();
    if (currentOrg) {
      this.router.navigate([link.replace('{OrgId}', currentOrg.id.id)]);
    }
  }
}
