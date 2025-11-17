import {Component, OnInit} from '@angular/core';
import {mergeMap, of, zip} from "rxjs";
import {StoreService} from "../../../store-management/services/store.service";
import {SelectedStoreService} from "../../../../shared/services/selected-store.service";
import {Store} from "../../../store-management/models/store";
import {ErrorService} from "../../../../shared/services/error.service";

@Component({
  selector: 'ngx-category-creation',
  standalone: false,
  templateUrl: './category-creation.component.html',
  styleUrls: ['./category-creation.component.scss']
})
export class CategoryCreationComponent implements OnInit {
  category = {};
  store: Store;

  constructor(private storeService: StoreService, private selectedStoreService: SelectedStoreService, private errorService: ErrorService) {
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
        },
        complete: () => {
        },
      });


  }

}
