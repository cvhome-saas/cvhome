import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {BoxesFacade} from "./facades/boxes.facade";
import {TableStateService} from "../../shared/table/table-state.service";

@Component({
  selector: 'boxes-table',
  standalone: false,
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
