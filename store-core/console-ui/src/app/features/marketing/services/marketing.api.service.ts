import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';

import {ContactRequest} from '@models/marketing';

@Injectable({providedIn: 'root'})
export class MarketingApi {
  sendContactMessage(request: ContactRequest): Observable<void> {
    void request;
    return of(void 0);
  }
}
