import {Component, OnInit, inject} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {PodListFacade} from '../facades/pod-list.facade';
import {TableStateService} from '../../shared/table/table-state.service';

@Component({
  selector: 'ngx-pod-list',
  standalone: false,
  templateUrl: './pod-list.component.html',
  styleUrls: ['./pod-list.component.scss'],
  providers: [PodListFacade, TableStateService]
})
export class PodListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(PodListFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
