import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {DomainReference} from "../model/domain-reference";

@Injectable({
  providedIn: 'root'
})
export class DomainReferenceService {

  constructor(private httpClient: HttpClient) {

  }

  save(domainReference: DomainReference): Observable<DomainReference> {
    return this.httpClient.post<DomainReference>("/user/api/v1/domain-reference", domainReference)
  }

  getAllDomainReferences(): Observable<DomainReference[]> {
    return this.httpClient.get<DomainReference[]>("/user/api/v1/domain-reference/get-all-domain-references")
  }
}
