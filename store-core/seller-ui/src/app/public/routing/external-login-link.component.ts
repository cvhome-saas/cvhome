import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-external-login-link',
  template: '',
})
export class ExternalLoginLinkComponent implements OnInit {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const redirectTo = encodeURIComponent(window.location.pathname);
      window.location.href = `${environment.LOGIN_URL}?redirectTo=${redirectTo}`;
    }
  }
}
