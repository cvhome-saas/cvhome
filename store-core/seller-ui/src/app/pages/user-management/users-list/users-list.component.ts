import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {UsersListFacade} from "./facades/users-list.facade";
import {TableStateService} from "../../shared/table/table-state.service";
import {UserStatusComponent} from './user-status.component';

@Component({
  selector: 'ngx-users-list',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule, UserStatusComponent],
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss'],
  providers: [UsersListFacade, TableStateService]
})
export class UsersListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(UsersListFacade);
  private readonly destroyRef = inject(DestroyRef);


  ngOnInit() {
    this.facade.init(this.destroyRef);
  }
}
