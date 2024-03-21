import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {
    Domain,
    DomainCertificateStatus,
    DomainOwnershipService,
    DomainType,
    ReferenceType,
    RegisterDomainResponse,
    RegisteredDomain
} from "../../../../../service/domain-ownership.service";
import {Store, StoreService} from "../../../../../service/store.service";

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
    stores: Store[] = [];


    constructor(private domainOwnershipService: DomainOwnershipService, private storeService: StoreService) {
        this.customDomainForm = new FormGroup({
            domain: new FormControl(null, [
                Validators.required,
                Validators.minLength(4),
                Validators.pattern('^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$')
            ]),
        });
        this.storeService.findAllStores().subscribe(it => this.stores = it)
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

    onReferenceChange(domain: Domain, value: any) {
        let refId = value.target.value;
        this.domainOwnershipService.changeReference({
            domain: domain,
            reference: {
                reference: refId,
                referenceType: ReferenceType.STORE
            }
        }).subscribe(it => {
            this.customDomainReferences.filter(it => it.domain.domain == domain.domain).forEach(d => d.reference = it.reference);
        })
    }

}
