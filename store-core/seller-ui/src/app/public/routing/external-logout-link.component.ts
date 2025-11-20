import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-external-logout-link',
  template: '',
})
export class ExternalLogoutLinkComponent implements OnInit {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.location.href = environment.LOGOUT_URL;
    }
  }
}
