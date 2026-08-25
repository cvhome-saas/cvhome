import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {SignUpService} from '@api/signup/sign-up.service';
import type {CreateOrgRequest, ReadableUser} from '@models/signup';

@Injectable({providedIn: 'root'})
export class ConsoleAuthApi {
  private readonly signUp = inject(SignUpService);

  /** Creates the organization and its first administrator. The form already carries the wire shape. */
  createAccount(request: CreateOrgRequest): Observable<ReadableUser> {
    return this.signUp.signUp(request);
  }
}
