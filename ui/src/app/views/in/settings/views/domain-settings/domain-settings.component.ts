import {AfterViewInit, Component} from '@angular/core';
import {DomainReference, DomainType} from "../../../../../model/domain-reference";
import {DomainReferenceService} from "../../../../../service/domain-reference.service";

@Component({
  selector: 'app-domain-settings',
  templateUrl: './domain-settings.component.html',
  styleUrls: ['./domain-settings.component.css']
})
export class DomainSettingsComponent implements AfterViewInit {
  subDomainReferences: DomainReference[] = [];
  customDomainReferences: DomainReference[] = [];

  constructor(private domainReferenceService: DomainReferenceService) {
  }

  ngAfterViewInit(): void {
    this.domainReferenceService.getAllDomainReferences().subscribe((it: DomainReference[]) => {
      this.subDomainReferences = it.filter((d: DomainReference): boolean => {
        return d.domainType == DomainType.SUB;
      });
      this.customDomainReferences = it.filter((d: DomainReference): boolean => {
        return d.domainType == DomainType.CUSTOM;
      });
    });
  }
}
