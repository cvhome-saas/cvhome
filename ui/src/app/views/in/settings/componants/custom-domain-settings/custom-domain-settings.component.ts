import {Component, Input} from '@angular/core';
import {DomainReference, DomainType} from "../../../../../model/domain-reference";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DomainReferenceService} from "../../../../../service/domain-reference.service";
import {environment} from "../../../../../../environment";
import {DnsLookupService} from "../../../../../service/dns-lookup.service";
import {map} from "rxjs";

@Component({
  selector: 'app-custom-domain-settings',
  templateUrl: './custom-domain-settings.component.html',
  styleUrls: ['./custom-domain-settings.component.css']
})
export class CustomDomainSettingsComponent {
  @Input()
  customDomainReferences: DomainReference[] = [];
  baseDomain = environment.BASE_DOMAIN;
  domainReferenceForm: FormGroup;


  constructor(private domainReferenceService: DomainReferenceService, private dnsLookupService: DnsLookupService) {
    this.domainReferenceForm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4)
      ]),
    });

  }


  onSubmit() {
    this.check();
    let value: DomainReference = this.domainReferenceForm.value;
    value.domainType = DomainType.CUSTOM;
    this.domainReferenceService.save(value).subscribe((it: DomainReference) => {
      this.customDomainReferences.push(it);
      this.domainReferenceForm.reset();
    });
  }

  check() {
    this.dnsLookupService.lookup(this.domainReferenceForm.value.domain)
      .pipe(map(it => it.Answer), map(it => {
        return it.filter(answer => answer.type == 5 && answer.data.includes(this.baseDomain)).length > 0
      }))
      .subscribe(it => {
        console.log(it);
      })
  }
}
