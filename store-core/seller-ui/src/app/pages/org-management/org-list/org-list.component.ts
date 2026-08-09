import {Component, OnInit, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {OrgListFacade} from "./facades/org-list.facade";
import {TableStateService} from "seller-core";

@Component({
  selector: 'ngx-org-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
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
