import {PageEvent} from "@swimlane/ngx-datatable";
import {Observable} from "rxjs";

export abstract class BaseTable<T> {
  public perPageSize = 10;

  protected constructor() {
  }

  private _params: PageRequest = {
    page: 0,
    count: this.perPageSize
  };

  get params(): PageRequest {
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
    number: 0,
    content: []
  };

  get page(): PageT<T> {
    return this._page;
  }

  abstract list(request: PageRequest): Observable<PageT<T>>;

  public trigger() {
    this._isLoading = true;
    this.list(this._params).subscribe({
      next: (it) => {
        this._page = it
      },
      error: (err) => {
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
}

export interface PageRequest {
  count: number,
  page: number
}

export class PageT<T> {
  size: number = 0;
  totalElements: number = 0;
  totalPages: number = 0;
  number: number = 0;
  content: T[] = [];
}
