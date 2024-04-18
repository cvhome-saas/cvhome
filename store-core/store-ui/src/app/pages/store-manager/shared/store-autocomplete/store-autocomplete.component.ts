import {AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {ManagerStoreId, Store} from "../../../../shared/domain/commons";
import {NbSelectComponent} from "@nebular/theme";
import {StoreService} from "../../../../shared/service/store.service";

@Component({
  selector: 'ngx-store-autocomplete',
  templateUrl: './store-autocomplete.component.html',
  styleUrls: ['./store-autocomplete.component.scss']
})
export class StoreAutocompleteComponent implements AfterViewInit {
  @ViewChild(NbSelectComponent)
  routeSelect: NbSelectComponent;
  @Output()
  onStore: EventEmitter<ManagerStoreId> = new EventEmitter<ManagerStoreId>()
  @Input()
  selectedItem: ManagerStoreId | undefined;
  @Input()
  selectable: boolean = true
  private lastStore: ManagerStoreId | undefined;
  stores: Store[] | undefined;

  changed($event: ManagerStoreId) {
    if ($event != undefined && $event.id != undefined && (this.lastStore == undefined || this.lastStore.id != $event.id)) {
      this.onStore.emit($event)
    }
    this.lastStore = $event
  }

  constructor(private cdr: ChangeDetectorRef, private storeService: StoreService) {
  }

  ngAfterViewInit(): void {
    this.storeService.list().subscribe(it => {
      this.stores = it.content;
      if (this.stores.length > 0) {
        if (this.selectedItem == undefined) {
          this.selectedItem = this.stores[0].id
        } else {
          let filtered: Store[] = this.stores.filter(it => it.id.id == this.selectedItem.id);
          if (filtered.length > 0) {
            this.selectedItem = filtered[0].id
          } else {
            this.selectedItem = undefined;
          }
        }
        this.cdr.detectChanges();
        this.changed(this.selectedItem)
      }
    })

  }
}
