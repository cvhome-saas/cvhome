import {Component, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {PagesFacade} from "./facades/pages.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'page-table',
  standalone: false,
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss'],
  providers: [PagesFacade, TableStateService]
})
export class PageComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(PagesFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
