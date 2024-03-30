import {Component} from '@angular/core';

@Component({
    selector: 'app-domain-settings',
    templateUrl: './domain-settings.component.html',
    styleUrls: ['./domain-settings.component.css']
})
export class DomainSettingsComponent /*implements AfterViewInit*/ {
    /*
        subDomainReferences: RegisteredDomain[] = [];
        customDomainReferences: RegisteredDomain[] = [];

        constructor(private domainOwnershipService: DomainOwnershipService) {
        }

        ngAfterViewInit(): void {
            this.domainOwnershipService.getRegisteredDomains().subscribe((it: RegisteredDomain[]) => {
                this.subDomainReferences = it.filter((d: RegisteredDomain): boolean => {
                    return d.domainType == DomainType.SAS_SUB;
                });
                this.customDomainReferences = it.filter((d: RegisteredDomain): boolean => {
                    return d.domainType == DomainType.SAS_CUSTOM;
                });
            });
        }
    */
}
