import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {BaseTable, PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';
import {Role, RolesService} from '../services/roles-service';
import {Router} from '@angular/router';
import {ErrorService} from '../../../shared/services/error.service';

@Component({
  selector: 'app-roles-list-component',
  templateUrl: './roles-list-component.html',
  standalone: false,
  styleUrl: './roles-list-component.scss',
})
export class RolesListComponent extends BaseTable<any> implements OnInit, AfterViewInit {
  protected readonly ColumnMode = ColumnMode;

  constructor(private rolesService: RolesService, private router: Router, private errorService: ErrorService) {
    super();
  }


  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.trigger();
  }


  override list(request: PageRequest): Observable<PageT<Role>> {
    return this.rolesService.list(request);
  }

  protected onEdit(row: Role) {
    this.router.navigate(["/home/roles/edit", row.id])
  }

  protected onDelete(row: Role) {
    this.rolesService.delete(row.id).subscribe({
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

