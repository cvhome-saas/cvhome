import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {GroupsListFacade} from '../facades/groups-list.facade';
import {TableStateService} from 'seller-core';
import {ActiveButtonComponent} from './active-button.component';

@Component({
  selector: 'ngx-groups-list',
  standalone: true,
  imports: [
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbIconModule,
    NbSpinnerModule,
    NgxDatatableModule,
    ActiveButtonComponent
  ],
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
