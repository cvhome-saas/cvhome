import {Component, Input} from '@angular/core';
import {DomainReference, DomainReferenceOrder, DomainType} from "../../../../../model/domain-reference";
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
    subDomainReferences: DomainReferenceOrder[] = [];
    baseDomain: string;
    domainReferenceForm: FormGroup;


    constructor(private domainReferenceService: DomainReferenceService) {
        this.baseDomain = environment.BASE_DOMAIN;
        this.domainReferenceForm = new FormGroup({
            domain: new FormControl(null, [
                Validators.required,
                Validators.minLength(4),
                Validators.pattern('^[a-zA-Z0-9]+$')
            ]),
        });

    }


    onSubmit() {
        if (this.domainReferenceForm.valid) {
            let value: DomainReference = this.domainReferenceForm.value;
            this.domainReferenceService.save({
                domain: value.domain + "." + this.baseDomain,
                domainType: DomainType.SUB
            }).subscribe((it: DomainReference) => {
                this.subDomainReferences.push({domainReference: it});
                this.domainReferenceForm.reset();
            });
        }
    }

}
