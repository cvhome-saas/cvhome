import {Component, Input} from '@angular/core';
import {environment} from "../../../../../../environment";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {
  DomainCertificateStatus,
  DomainOwnershipService,
  DomainType,
  RegisterDomainResponse,
  RegisteredDomain
} from "../../../../../service/domain-ownership.service";

@Component({
  selector: 'app-sub-domain-settings',
  templateUrl: './sub-domain-settings.component.html',
  styleUrls: ['./sub-domain-settings.component.css']
})
export class SubDomainSettingsComponent {
  @Input()
  subDomainReferences: RegisteredDomain[] = [];
  baseDomain: string;
  subDomainFormm: FormGroup;


  constructor(private domainOwnershipService: DomainOwnershipService) {
    this.baseDomain = environment.BASE_DOMAIN;
    this.subDomainFormm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^[a-zA-Z0-9]+$')
      ]),
    });

  }


  onSubmit() {
    if (this.subDomainFormm.valid) {
      let value: any = this.subDomainFormm.value;
      this.domainOwnershipService.register({
        domain: {
          domain: value.domain + "." + this.baseDomain
        },
        domainType: DomainType.SAS_SUB
      }).subscribe((it: RegisterDomainResponse) => {
        this.subDomainReferences.push({
          domain: it.domain,
          domainType: DomainType.SAS_SUB,
          reference: it.reference,
          certificateStatus: DomainCertificateStatus.ACTIVE_CERTIFICATE_GENERATED,
        });
        this.subDomainFormm.reset();
      });
    }
  }

}
