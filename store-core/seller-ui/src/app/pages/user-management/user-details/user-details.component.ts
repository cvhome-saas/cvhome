import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

import {UserService} from '../../shared/services/user.service';
import {map, mergeMap} from "rxjs/operators";
import {ErrorService} from "../../shared/services/error.service";

@Component({
  selector: 'ngx-user-details',
  standalone: false,
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent implements OnInit {
  user: any = {};
  loadingInfo = false;

  constructor(
    private activatedRoute: ActivatedRoute,
    private userService: UserService,
    private errorService: ErrorService
  ) {
  }

  ngOnInit() {
    this.activatedRoute.params.pipe(map(it => it['id']), mergeMap(it => this.userService.getUser(it)))
      .subscribe(res => {
        this.user = res;
        this.loadingInfo = false;
      }, err => {
        this.loadingInfo = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      })

  }

}
