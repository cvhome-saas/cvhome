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
import {Store, StoreService} from "../../../../../service/store.service";

@Component({
  selector: 'app-sub-domain-settings',
  templateUrl: './sub-domain-settings.component.html',
  styleUrls: ['./sub-domain-settings.component.css']
})
export class SubDomainSettingsComponent {
  @Input()
  subDomainReferences: RegisteredDomain[] = [];
  baseDomain: string;
  subDomainForm: FormGroup;
  stores: Store[] = [];

  constructor(private domainOwnershipService: DomainOwnershipService, private storeService: StoreService) {
    this.baseDomain = environment.BASE_DOMAIN;
    this.subDomainForm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4),
        Validators.pattern('^[a-zA-Z0-9]+$')
      ]),
    });
    this.storeService.findAllStores().subscribe(it => this.stores = it)
  }


  onSubmit() {
    if (this.subDomainForm.valid) {
      let value: any = this.subDomainForm.value;
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
        this.subDomainForm.reset();
      });
    }
  }

}
