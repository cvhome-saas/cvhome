import {AfterViewInit, Component} from '@angular/core';
import {
  DomainReferenceOrder,
  DomainReferenceService,
  DomainType
} from "../../../../../service/domain-reference.service";

@Component({
    selector: 'app-domain-settings',
    templateUrl: './domain-settings.component.html',
    styleUrls: ['./domain-settings.component.css']
})
export class DomainSettingsComponent implements AfterViewInit {
    subDomainReferences: DomainReferenceOrder[] = [];
    customDomainReferences: DomainReferenceOrder[] = [];

    constructor(private domainReferenceService: DomainReferenceService) {
    }

    ngAfterViewInit(): void {
        this.domainReferenceService.getAllDomainReferencesOrders().subscribe((it: DomainReferenceOrder[]) => {
            this.subDomainReferences = it.filter((d: DomainReferenceOrder): boolean => {
                return d.domainReference.domainType == DomainType.SUB;
            });
            this.customDomainReferences = it.filter((d: DomainReferenceOrder): boolean => {
                return d.domainReference.domainType == DomainType.CUSTOM;
            });
        });
    }
}
