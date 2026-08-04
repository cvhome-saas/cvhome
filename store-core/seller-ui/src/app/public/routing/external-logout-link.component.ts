import {Component, OnInit, PLATFORM_ID, inject} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-external-logout-link',
  standalone: true,
  template: '',
})
export class ExternalLogoutLinkComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.location.href = environment.LOGOUT_URL;
    }
  }
}
