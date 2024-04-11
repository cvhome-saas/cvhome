import {AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Output, ViewChild} from '@angular/core';
import {ManagerStoreId, Store} from "../../../../shared/domain/commons";
import {NbSelectComponent} from "@nebular/theme";
import {StoreService} from "../../../../shared/service/store.service";

@Component({
  selector: 'ngx-store-autocomplete',
  templateUrl: './store-autocomplete.component.html',
  styleUrls: ['./store-autocomplete.component.scss']
})
export class StoreAutocompleteComponent implements AfterViewInit {
  @ViewChild(NbSelectComponent) routeSelect: NbSelectComponent;

  @Output() onStore: EventEmitter<ManagerStoreId> = new EventEmitter<ManagerStoreId>()
  selectedItem: ManagerStoreId | undefined;
  stores: Store[]|undefined ;

  changed($event: ManagerStoreId) {
    this.onStore.emit($event)
  }

  constructor(private cdr: ChangeDetectorRef, private storeService: StoreService) {
  }

  ngAfterViewInit(): void {
    this.storeService.list().subscribe(it => {
      this.stores = it.content;
      if (this.stores.length > 0) {
        this.selectedItem = this.stores[0].id
        this.cdr.detectChanges();
        this.changed(this.selectedItem)
      }
    })

  }
}
