import {Component, Input} from '@angular/core';

import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {UserService} from "../../shared/services/user.service";

@Component({
  selector: 'ngx-user-status',
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
  ) {
  }


  clicked() {
    if (this.rowData.active) {
      this.userService.disable(this.rowData.id, this.store)
        .subscribe(data => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_DISABLED'));
        });
    } else {
      this.userService.enable(this.rowData.id, this.store)
        .subscribe(data => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_ENABLED'));
        });
    }
    this.rowData.active = !this.rowData.active;

  }
}
