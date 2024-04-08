import {AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Output, ViewChild} from '@angular/core';
import {ManagerStoreId, Store} from "../../../../shared/domain/commons";
import {NbSelectComponent} from "@nebular/theme";

@Component({
  selector: 'ngx-store-selector',
  templateUrl: './store-selector.component.html',
  styleUrls: ['./store-selector.component.scss']
})
export class StoreSelectorComponent implements AfterViewInit {
  @ViewChild(NbSelectComponent) routeSelect: NbSelectComponent;

  @Output() storeSelectionChange: EventEmitter<ManagerStoreId> = new EventEmitter<ManagerStoreId>()
  selectedItem: ManagerStoreId | undefined;
  stores: Store[] = [
    {
      id: {
        id: "1"
      },
      name: "store1",
      owner: {
        id: "1"
      }
    },
    {
      id: {
        id: "2"
      },
      name: "store2",
      owner: {
        id: "1"
      }
    }
  ];

  changed($event: ManagerStoreId) {
    this.storeSelectionChange.emit($event)
  }

  constructor(private cdr: ChangeDetectorRef) {
  }

  ngAfterViewInit(): void {
    this.selectedItem = this.stores[1].id
    this.cdr.detectChanges();
    this.changed(this.selectedItem)
  }
}
