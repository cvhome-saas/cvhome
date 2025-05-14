import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {OrgService} from '../services/org.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../../shared/services/error.service";
import {BaseTable, PageT, StorePageRequest} from "../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-org-list',
  standalone: false,
  templateUrl: './org-list.component.html',
  styleUrls: ['./org-list.component.scss']
})
export class OrgListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private orgService: OrgService,
    private router: Router,
    errorService: ErrorService
  ) {
    super(null, errorService)
  }

  ngOnInit() {
    this.isInitialized = true;
    this.trigger();
  }

  override list(request: StorePageRequest): Observable<PageT<any>> {
    if (!this.isInitialized) {
      return of();
    }
    return this.orgService.getListOfOrg(request)
  }

  onEdit(row) {
    this.router.navigate(['pages/org-management/org/', row.id.id]);

  }

}
