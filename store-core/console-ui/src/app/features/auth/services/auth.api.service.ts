import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';

import {SignUpRequest} from '@models/auth';

@Injectable({providedIn: 'root'})
export class ConsoleAuthApi {
  createAccount(request: SignUpRequest): Observable<void> {
    void request;
    return of(void 0);
  }
}
