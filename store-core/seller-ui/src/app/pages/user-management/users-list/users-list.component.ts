import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {UsersListFacade} from "./facades/users-list.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'ngx-users-list',
  standalone: false,
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
