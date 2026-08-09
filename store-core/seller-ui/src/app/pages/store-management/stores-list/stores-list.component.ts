import {Component, OnInit, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {StoresListFacade} from "./facades/stores-list.facade";
import {TableStateService} from "seller-core";

@Component({
  selector: 'ngx-stores-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
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
