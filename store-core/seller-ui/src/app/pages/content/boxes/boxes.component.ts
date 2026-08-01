import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {BoxesFacade} from "./facades/boxes.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'app-boxes-table',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
  templateUrl: './boxes.component.html',
  styleUrls: ['./boxes.component.scss'],
  providers: [BoxesFacade, TableStateService]
})
export class BoxesComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(BoxesFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
