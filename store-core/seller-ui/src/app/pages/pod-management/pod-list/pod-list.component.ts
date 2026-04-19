import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {PodService} from '../../store-management/services/pod.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {ErrorService} from "../../shared/services/error.service";
import {BaseTable, PageRequest, PageT} from "../../common/BaseTable";
import {Observable, of} from "rxjs";

@Component({
  selector: 'ngx-pod-list',
  standalone: false,
  templateUrl: './pod-list.component.html',
  styleUrls: ['./pod-list.component.scss']
})
export class PodListComponent extends BaseTable<any> implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  private isInitialized: boolean = false;

  constructor(
    private podService: PodService,
    private router: Router,
    errorService: ErrorService
  ) {
    super(null, errorService)
  }

  ngOnInit() {
    this.isInitialized = true;
    this.trigger();
  }

  override list(params: PageRequest): Observable<PageT<any>> {
    if (!this.isInitialized) {
      return of();
    }
    return this.podService.findAllPods(params)
  }

  onEdit(row) {
    this.router.navigate(['pages/pod-management/pod/', row.id.id]);
  }

  onDelete(row) {
    this.podService.delete(row.id.id).subscribe(() => {
      this.trigger();
    });
  }

}
