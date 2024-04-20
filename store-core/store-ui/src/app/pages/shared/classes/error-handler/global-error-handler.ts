import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {AuthService} from "../../../../shared/service/auth.service";

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor(
    private injector: Injector,
    private authService: AuthService) {
  }

  handleError(error: Error) {
    if (error instanceof HttpErrorResponse) {
      if (!navigator.onLine) {
        console.log('Error offline ' + error.status);
      } else {
        console.log('Error status ' + error.status);
        if (error.status === 401) {
          this.authService.logout();
        } else if (error.status === 404) {

        } else if (error.status === 0) {
          window.location.href = '/assets/static/error.html';
        }
      }
    } else {
    }
  }
}
