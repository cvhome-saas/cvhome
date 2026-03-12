import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {BaseTable, PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';
import {ClientsService, ClientSummary} from '../services/clients-service';
import {Router} from '@angular/router';
import {ErrorService} from '../../../shared/services/error.service';

@Component({
  selector: 'app-clients-list-component',
  templateUrl: './clients-list-component.html',
  standalone: false,
  styleUrl: './clients-list-component.scss',
})
export class ClientsListComponent extends BaseTable<any> implements OnInit, AfterViewInit {
  protected readonly ColumnMode = ColumnMode;

  constructor(private clientsService: ClientsService, private router: Router, private errorService: ErrorService) {
    super();
  }


  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.trigger();
  }


  override list(request: PageRequest): Observable<PageT<ClientSummary>> {
    return this.clientsService.list(request);
  }

  protected onEdit(row: ClientSummary) {
    this.router.navigate(["/home/clients/edit", row.id])
  }

  protected onDelete(row: ClientSummary) {
    this.clientsService.delete(row.id).subscribe({
      next: (it) => {
        this.errorService.success("COMMON.DELETE_SUCCESS")
        this.trigger();
      },
      error: (err) => {
        console.log(err)
        this.errorService.error('COMMON.DELETE_FAILED', err.error.detail);
      }
    });
  }
}

