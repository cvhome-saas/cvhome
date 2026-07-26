import {Component, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {StoresListFacade} from "./facades/stores-list.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'ngx-stores-list',
  standalone: false,
  templateUrl: './stores-list.component.html',
  styleUrls: ['./stores-list.component.scss'],
  providers: [StoresListFacade, TableStateService]
})
export class StoresListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(StoresListFacade);

  ngOnInit() {
    this.facade.init();
  }
}
