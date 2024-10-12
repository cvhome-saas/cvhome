import {Injectable} from '@angular/core';
import {EvnLoaderService} from "./evn-loader.service";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SignUpForm, SignUpResponse} from "../domain/types";

@Injectable({
  providedIn: 'root'
})
export class SignUpService {
  private readonly backendUrl: string | undefined;

  constructor(private eventLoaderService: EvnLoaderService, private httpClient: HttpClient) {
    this.backendUrl = this.eventLoaderService.backendUrl
  }

  signUp(signUpForm: SignUpForm): Observable<SignUpResponse> {
    return this.httpClient.post<SignUpResponse>("/manager/api/v1/user-account/public/create", signUpForm);
  }
}
