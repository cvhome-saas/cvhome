import {Component, Input} from '@angular/core';
import {DomainReference, DomainType} from "../../../../../model/domain-reference";
import {environment} from "../../../../../../environment";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DomainReferenceService} from "../../../../../service/domain-reference.service";

@Component({
  selector: 'app-sub-domain-settings',
  templateUrl: './sub-domain-settings.component.html',
  styleUrls: ['./sub-domain-settings.component.css']
})
export class SubDomainSettingsComponent {
  @Input()
  subDomainReferences: DomainReference[] = [];
  baseDomain: string;
  domainReferenceForm: FormGroup;


  constructor(private domainReferenceService: DomainReferenceService) {
    this.baseDomain = environment.BASE_DOMAIN;
    this.domainReferenceForm = new FormGroup({
      domain: new FormControl(null, [
        Validators.required,
        Validators.minLength(4)
      ]),
    });

  }


  onSubmit() {
    let value: DomainReference = this.domainReferenceForm.value;
    value.domainType = DomainType.SUB;
    value.domain = value.domain + "." + this.baseDomain;
    this.domainReferenceService.save(value).subscribe((it: DomainReference) => {
      this.subDomainReferences.push(it);
      this.domainReferenceForm.reset();
    });
  }

}
