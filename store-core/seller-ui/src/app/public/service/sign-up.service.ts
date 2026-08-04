import { Injectable, inject } from '@angular/core';
import {Observable} from "rxjs";
import {SignUpForm, SignUpResponse} from "../domain/types";
import {CrudService} from "../../pages/shared/services/crud.service";

@Injectable({
  providedIn: 'root'
})
export class SignUpService {
  private readonly crudService = inject(CrudService);


  signUp(signUpForm: SignUpForm): Observable<SignUpResponse> {
    return this.crudService.post("/control-plane/api/v1/user-account/public/create", signUpForm);
  }
}
