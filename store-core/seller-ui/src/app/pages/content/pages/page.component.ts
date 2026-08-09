import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {PagesFacade} from "./facades/pages.facade";
import {TableStateService} from "seller-core";

@Component({
  selector: 'app-page-table',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss'],
  providers: [PagesFacade, TableStateService]
})
export class PageComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(PagesFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
