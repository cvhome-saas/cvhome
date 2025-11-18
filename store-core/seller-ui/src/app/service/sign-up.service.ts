import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SignUpForm, SignUpResponse} from "../domain/types";

@Injectable({
  providedIn: 'root'
})
export class SignUpService {

  constructor(private httpClient: HttpClient) {
  }

  signUp(signUpForm: SignUpForm): Observable<SignUpResponse> {
    return this.httpClient.post<SignUpResponse>("/control-plane/api/v1/user-account/public/create", signUpForm);
  }
}
