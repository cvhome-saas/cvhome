import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {OrgStoresListFacade} from "./facades/org-stores-list.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'ngx-org-stores-list',
  standalone: false,
  templateUrl: './org-stores-list.component.html',
  styleUrls: ['./org-stores-list.component.scss'],
  providers: [OrgStoresListFacade, TableStateService]
})
export class OrgStoresListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(OrgStoresListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.facade.init(this.destroyRef);
  }
}
