import {Component} from '@angular/core';


@Component({
    selector: 'app-sub-domain-settings',
    templateUrl: './sub-domain-settings.component.html',
    styleUrls: ['./sub-domain-settings.component.css']
})
export class SubDomainSettingsComponent /*implements OnInit */{
/*    @Input()
    subDomainReferences: RegisteredDomain[] = [];
    baseDomain: string = '';
    subDomainForm: FormGroup;
    stores: Store[] = [];

    constructor(private domainOwnershipService: DomainOwnershipService, private storeService: StoreService) {
        this.subDomainForm = new FormGroup({
            domain: new FormControl(null, [
                Validators.required,
                Validators.minLength(4),
                Validators.pattern('^[a-zA-Z0-9]+$')
            ]),
        });
    }

    ngOnInit(): void {
        this.storeService.findAllStores().subscribe(it => this.stores = it)
        this.domainOwnershipService.defaultDomain().subscribe(it => this.baseDomain = it.domain)
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

    onReferenceChange(domain: Domain, value: any) {
        let refId = value.target.value;
        this.domainOwnershipService.changeReference({
            domain: domain,
            reference: {
                reference: refId,
                referenceType: ReferenceType.STORE
            }
        }).subscribe(it => {
            this.subDomainReferences.filter(it => it.domain.domain == domain.domain).forEach(d => d.reference = it.reference);
        })
    }
*/}
