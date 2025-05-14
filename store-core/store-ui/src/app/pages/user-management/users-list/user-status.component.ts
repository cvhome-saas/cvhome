import {Component, Input} from '@angular/core';

import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {UserService} from "../../../shared/services/user.service";
import {ErrorService} from "../../../shared/services/error.service";

@Component({
  selector: 'ngx-user-status',
  standalone: false,
  template: `
    <nb-checkbox [checked]="rowData.active" (checkedChange)="clicked()"/>`,
})
export class UserStatusComponent {
  @Input() store: string;
  @Input() rowData: any;

  constructor(
    private userService: UserService,
    private translate: TranslateService,
    private toastr: NbToastrService,
    private errorService: ErrorService
  ) {
  }


  clicked() {
    if (this.rowData.active) {
      this.userService.disable(this.rowData.id, this.store)
        .subscribe(data => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_DISABLED'));
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    } else {
      this.userService.enable(this.rowData.id, this.store)
        .subscribe(data => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_ENABLED'));
        }, err => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        });
    }
    this.rowData.active = !this.rowData.active;

  }
}
