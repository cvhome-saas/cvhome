import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {environment} from "../../../../../../environment";
import {DnsLookupService} from "../../../../../service/dns-lookup.service";
import {map, mergeMap, Observable} from "rxjs";
import {
  DomainCertificateStatus,
  DomainOwnershipService,
  DomainType,
  RegisterDomainResponse,
  RegisteredDomain
} from "../../../../../service/domain-ownership.service";

@Component({
  selector: 'app-custom-domain-settings',
  templateUrl: './custom-domain-settings.component.html',
  styleUrls: ['./custom-domain-settings.component.css']
})
export class CustomDomainSettingsComponent {
  @Input()
  customDomainReferences: RegisteredDomain[] = [];
  baseDomain: string = environment.BASE_DOMAIN;
  domainReferenceForm: FormGroup;
  errMessage: string = '';


  constructor(private domainOwnershipService: DomainOwnershipService, private dnsLookupService: DnsLookupService) {
    this.domainReferenceForm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$')
      ]),
    });
  }


  onSubmit() {
    if (this.domainReferenceForm.valid) {
      let value: any = this.domainReferenceForm.value;
      this.checkCNAMEHost(value.domain)
        .pipe(mergeMap(it => this.domainOwnershipService.register({
          domain: {
            domain: value.domain
          },
          domainType: DomainType.SAS_CUSTOM
        })))
        .subscribe({
          next: (it: RegisterDomainResponse) => {
            this.errMessage = '';
            this.customDomainReferences.push({
              domain: it.domain,
              domainType: DomainType.SAS_CUSTOM,
              reference: it.reference,
              certificateStatus: DomainCertificateStatus.INITIATED
            });
          },
          error: (e: Error) => this.errMessage = e.message,
          complete: () => this.domainReferenceForm.reset()
        });
    }
  }

  checkCNAMEHost(domain: string): Observable<boolean> {
    return this.dnsLookupService.lookup(domain)
      .pipe(
        map(it => it.Answer),
        map(it => (it != null && it.filter(answer => answer.type == 5 && answer.data.includes(this.baseDomain)).length > 0)),
      )
  }
}
