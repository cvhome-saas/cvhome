import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {BaseTable, PageRequest, PageT} from '../../../../common/BaseTable';
import {Observable} from 'rxjs';
import {User, UsersService} from '../services/users-service';
import {Router} from '@angular/router';
import {ErrorService} from '../../../shared/services/error.service';

@Component({
  selector: 'app-users-list-component',
  templateUrl: './users-list-component.html',
  standalone: false,
  styleUrl: './users-list-component.scss',
})
export class UsersListComponent extends BaseTable<any> implements OnInit, AfterViewInit {
  protected readonly ColumnMode = ColumnMode;

  constructor(private usersService: UsersService, private router: Router, private errorService: ErrorService) {
    super();
  }


  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.trigger();
  }


  override list(request: PageRequest): Observable<PageT<User>> {
    return this.usersService.list(request);
  }

  protected onEdit(row: User) {
    this.router.navigate(["/home/users/edit", row.id])
  }

  protected onDelete(row: User) {
    this.usersService.delete(row.id).subscribe({
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

