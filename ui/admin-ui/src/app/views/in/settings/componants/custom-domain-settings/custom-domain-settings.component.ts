import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {environment} from "../../../../../../environment";
import {DnsLookupService} from "../../../../../service/dns-lookup.service";
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
  customDomainForm: FormGroup;
  errMessage: string = '';


  constructor(private domainOwnershipService: DomainOwnershipService, private dnsLookupService: DnsLookupService) {
    this.customDomainForm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$')
      ]),
    });
  }


  onSubmit() {
    if (this.customDomainForm.valid) {
      let value: any = this.customDomainForm.value;
      this.domainOwnershipService.register({
        domain: {
          domain: value.domain
        },
        domainType: DomainType.SAS_CUSTOM
      }).subscribe({
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
        complete: () => this.customDomainForm.reset()
      });
    }
  }
}
