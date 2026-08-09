import {Component, OnInit, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {ColumnMode, NgxDatatableModule} from '@swimlane/ngx-datatable';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule} from '@nebular/theme';
import {PodListFacade} from '../facades/pod-list.facade';
import {TableStateService} from 'seller-core';

@Component({
  selector: 'ngx-pod-list',
  standalone: true,
  imports: [RouterLink, TranslateModule, NbButtonModule, NbCardModule, NbIconModule, NbSpinnerModule, NgxDatatableModule],
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
