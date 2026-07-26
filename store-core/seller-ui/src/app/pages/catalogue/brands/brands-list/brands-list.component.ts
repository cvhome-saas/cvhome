import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {BrandsListFacade} from '../facades/brands-list.facade';
import {TableStateService} from '../../../shared/table/table-state.service';

@Component({
  selector: 'ngx-brands-list',
  standalone: false,
  templateUrl: './brands-list.component.html',
  styleUrls: ['./brands-list.component.scss'],
  providers: [BrandsListFacade, TableStateService]
})
export class BrandsListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(BrandsListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
