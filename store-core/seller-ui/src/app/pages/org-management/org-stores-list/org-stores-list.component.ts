import {Component, OnInit} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../shared/services/error.service";
import {OrgService} from "../services/org.service";
import {Org} from "../model/org";
import {ActivatedRoute, Router} from "@angular/router";
import {map} from "rxjs/operators";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {mergeMap, Observable, of} from "rxjs";

@Component({
  selector: 'ngx-org-stores-list',
  standalone: false,
  templateUrl: './org-stores-list.component.html',
  styleUrls: ['./org-stores-list.component.scss']
})
export class OrgStoresListComponent extends BaseTable<any> implements OnInit {
  org: Org
  selectedItem = '2';
  sidemenuLinks = [
    {
      id: '0',
      title: 'COMPONENTS.UPDATE_ORG',
      key: 'COMPONENTS.UPDATE_ORG',
      link: '/pages/org-management/org/{OrgId}'
    }, {
      id: '1',
      title: 'COMPONENTS.CHANGE_PASSWORD',
      key: 'COMPONENTS.CHANGE_PASSWORD',
      link: '/pages/org-management/org/{OrgId}/change-password'
    }, {
      id: '2',
      title: 'COMPONENTS.STORES',
      key: 'COMPONENTS.STORES',
      link: '/pages/org-management/org/{OrgId}/stores'
    }
  ];
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private orgService: OrgService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    errorService: ErrorService
  ) {
    super(null, errorService)
  }

  route(link) {
    this.router.navigate([link.replace("{OrgId}", this.org.id.id)]);
  }

  ngOnInit() {
    this.activatedRoute.params.pipe(map(it => it['id']), mergeMap(it => this.orgService.getOrg(it)))
      .subscribe(res => {
        this.org = res;
        this.isInitialized = true;
        this.trigger();
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });


  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!this.isInitialized) {
      return of();
    }
    this.params['id'] = this.org.id.id;
    return this.orgService.getOrgStoresList(this.params)
  }


}
