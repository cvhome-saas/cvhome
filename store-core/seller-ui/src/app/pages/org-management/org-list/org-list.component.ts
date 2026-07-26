import {Component, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {OrgListFacade} from "./facades/org-list.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'ngx-org-list',
  standalone: false,
  templateUrl: './org-list.component.html',
  styleUrls: ['./org-list.component.scss'],
  providers: [OrgListFacade, TableStateService]
})
export class OrgListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(OrgListFacade);

  ngOnInit() {
    this.facade.init();
  }
}
