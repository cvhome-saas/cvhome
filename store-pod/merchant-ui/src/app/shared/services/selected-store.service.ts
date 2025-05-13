import {Injectable} from '@angular/core';
import {StoreService} from './store.service';
import {Store} from '../models/commons';
import {map, Observable, of, tap} from 'rxjs';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class SelectedStoreService {
  public static STORE_ID_KEY = "Store-Id";
  private _stores: Store[] | undefined = undefined;

  constructor(private storeService: StoreService) {
  }

  public select(storeId: string): void {
    localStorage.setItem(SelectedStoreService.STORE_ID_KEY, storeId);
  }

  current(): Observable<string> {
    return this.getAllStores().pipe(map(it => {
      let current = localStorage.getItem(SelectedStoreService.STORE_ID_KEY);
      if (current && current != '') {
        if (it.filter(s => s.id.id == current).length > 0) {
          return current;
        }
      }
      let newCurrent = undefined;
      if (it.length > 0) {
        newCurrent = it[0].id.id;
      }
      this.select(newCurrent);
      return newCurrent
    }));


  }

  private getAllStores(): Observable<Store[]> {
    if (this._stores == undefined) {
      return this.storeService.list()
        .pipe(map(it => it.content), tap((it: Store[]) => this._stores = it));
    } else {
      return of(this._stores);
    }

  }


}
