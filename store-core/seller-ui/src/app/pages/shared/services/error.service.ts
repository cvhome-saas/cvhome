import {Injectable} from '@angular/core';

import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";

@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor(
    private toastrService: NbToastrService,
    private translateService: TranslateService
  ) {
  }

  success(code: string) {
    this.toastrService.success(this.translateService.instant(code));
  }

  warning(code: string) {
    this.toastrService.warning(this.translateService.instant(code));
  }

  error(errorCode: string, code: string) {
    this.toastrService.danger(this.translateService.instant(errorCode));
    console.log('Application error [' + errorCode + ']' + code != null ? code : '');
  }

  handleError(err: Error) {

  }

}
