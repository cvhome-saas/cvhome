import {PageEvent} from "@swimlane/ngx-datatable";
import {Observable} from "rxjs";
import {SelectedStoreService} from "../shared/services/selected-store.service";
import {ErrorService} from "../shared/services/error.service";

export abstract class BaseTable<T> {
  public perPageSize = 10;

  protected constructor(private _selectedStoreService: SelectedStoreService,
                        private _errorService: ErrorService,
  ) {

    if (_selectedStoreService) {
      this._selectedStoreService.current().subscribe({
        next: (it) => {
          this.onStoreEvent(it)
        },
        error: err => {
          this._errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
        }
      });
    }
  }

  private _params: StorePageRequest = {
    page: 0,
    count: this.perPageSize
  };

  get params(): StorePageRequest {
    return this._params;
  }

  private _isLoading: boolean = false;

  get isLoading(): boolean {
    return this._isLoading;
  }

  private _page: PageT<T> = {
    size: 0,
    totalElements: 0,
    totalPages: 0,
    pageNumber: 0,
    content: []
  };

  get page(): PageT<T> {
    return this._page;
  }

  get selectedStoreService(): SelectedStoreService {
    return this._selectedStoreService;
  }

  get errorService(): ErrorService {
    return this._errorService;
  }

  abstract list(request: StorePageRequest): Observable<PageT<T>>;

  public trigger() {
    this._isLoading = true;
    this.list(this._params).subscribe({
      next: (it) => {
        this._page = it
      },
      error: (err) => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
        this._isLoading = false;
      },
      complete: () => {
        this._isLoading = false;
      }
    })
  }

  onPageEvent(event: PageEvent) {
    if (this._params.page != event.offset) {
      this._params.page = event.offset;
      this.trigger();
    }
  }

  onStoreEvent(store: string) {
    if (this._params.store != store) {
      this._params.store = store;
      this.trigger();
    }
  }
}

export interface PageRequest {
  count: number,
  page: number
}

export interface StorePageRequest extends PageRequest {
  store?: string,
}

export class PageT<T> {
  size: number = 0;
  totalElements: number = 0;
  totalPages: number = 0;
  pageNumber: number = 0;
  content: T[] = [];
}
