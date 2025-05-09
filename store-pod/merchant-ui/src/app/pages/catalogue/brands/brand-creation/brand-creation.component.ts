import {Component, OnInit} from '@angular/core';
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {Store} from "../../../store-management/models/store";
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../../store-management/services/store.service";
import {ErrorService} from "../../../../shared/services/error.service";

@Component({
  selector: 'ngx-brand-creation',
  standalone: false,
  templateUrl: './brand-creation.component.html',
  styleUrls: ['./brand-creation.component.scss']
})
export class BrandCreationComponent implements OnInit {
  brand = {};
  store: Store;

  constructor(private selectedStoreService: SelectedStoreService,
              private storeService: StoreService,
              private errorService: ErrorService) {

  }


  ngOnInit() {
    zip([this.selectedStoreService.current()])
      .pipe(mergeMap(([selectedStore]) => {
        return zip(of(selectedStore), this.storeService.getStore(selectedStore));
      }))
      .subscribe({
        next: ([selectedStore, store]) => {
          this.store = store;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });

  }
}
