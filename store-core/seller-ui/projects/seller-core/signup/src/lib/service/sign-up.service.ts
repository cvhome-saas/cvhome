import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {SignUpForm, SignUpResponse} from "../domain/types";
import {CrudService} from 'seller-core';

/** Base path for tenancy's public signup endpoint. */
export const SIGNUP_API_BASE = '/tenancy/api/v1/signup/public';

@Injectable({
  providedIn: 'root'
})
export class SignUpService {
  private readonly crudService = inject(CrudService);


  signUp(signUpForm: SignUpForm): Observable<SignUpResponse> {
    return this.crudService.post(`${SIGNUP_API_BASE}/create`, signUpForm);
  }
}
