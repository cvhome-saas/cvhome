import {Component, OnInit, inject, DestroyRef} from '@angular/core';
import {ColumnMode} from '@swimlane/ngx-datatable';
import {TypesListFacade} from '../facades/types-list.facade';
import {TableStateService} from '../../../shared/table/table-state.service';

@Component({
  selector: 'ngx-types-list',
  standalone: false,
  templateUrl: './types-list.component.html',
  styleUrls: ['./types-list.component.scss'],
  providers: [TypesListFacade, TableStateService]
})
export class TypesListComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(TypesListFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
