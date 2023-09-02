import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {
  DomainReference,
  DomainReferenceOrder,
  DomainReferenceService, DomainType
} from "../../../../../service/domain-reference.service";
import {environment} from "../../../../../../environment";
import {DnsLookupService} from "../../../../../service/dns-lookup.service";
import {map, mergeMap, Observable, of, throwError} from "rxjs";

@Component({
    selector: 'app-custom-domain-settings',
    templateUrl: './custom-domain-settings.component.html',
    styleUrls: ['./custom-domain-settings.component.css']
})
export class CustomDomainSettingsComponent {
    @Input()
    customDomainReferences: DomainReferenceOrder[] = [];
    baseDomain: string = environment.BASE_DOMAIN;
    domainReferenceForm: FormGroup;
    errMessage: string = '';


    constructor(private domainReferenceService: DomainReferenceService, private dnsLookupService: DnsLookupService) {
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
            let value: DomainReference = this.domainReferenceForm.value;
            value.domainType = DomainType.CUSTOM;
            this.checkHost(value.domain)
                .pipe(mergeMap(it => this.domainReferenceService.save(value)))
                .subscribe({
                    next: (it) => {
                        this.errMessage = '';
                        this.customDomainReferences.push({domainReference: it})
                    },
                    error: (e: Error) => this.errMessage = e.message,
                    complete: () => this.domainReferenceForm.reset()
                });
        }
    }

    checkHost(domain: string): Observable<boolean> {
        return this.dnsLookupService.lookup(domain)
            .pipe(
                map(it => it.Answer),
                map(it => (it != null && it.filter(answer => answer.type == 5 && answer.data.includes(this.baseDomain)).length > 0)),
                mergeMap(it => it ? of(it) : throwError(() =>
                    new Error(`Host not pointing to our server please check <a target="_blank" href="${this.dnsLookupService.getRequestUrl(domain)}">here</a>`)))
            )
    }

    orderHttps(d: DomainReference) {
        if (d.id) {
            this.domainReferenceService.orderHttps(d.id).subscribe((it: DomainReferenceOrder) => {
                let index = this.customDomainReferences.findIndex((obj => obj.domainReference.id == d.id));
                this.customDomainReferences[index] = it;
            });
        }
    }
}
