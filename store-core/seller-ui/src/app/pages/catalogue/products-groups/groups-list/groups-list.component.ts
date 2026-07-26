import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {GroupsListFacade} from '../facades/groups-list.facade';
import {TableStateService} from '../../../shared/table/table-state.service';

@Component({
  selector: 'ngx-groups-list',
  standalone: false,
  templateUrl: './groups-list.component.html',
  styleUrls: ['./groups-list.component.scss'],
  providers: [GroupsListFacade, TableStateService]
})
export class GroupsListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(GroupsListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
