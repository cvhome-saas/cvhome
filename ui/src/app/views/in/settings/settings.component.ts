import {AfterViewInit, Component} from '@angular/core';
import {DomainReferenceService} from "../../../service/domain-reference.service";
import {DomainReference, DomainType} from "../../../model/domain-reference";

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements AfterViewInit {
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
