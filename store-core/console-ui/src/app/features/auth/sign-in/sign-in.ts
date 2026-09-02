import {Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {UI_KIT_CONFIG} from '@cvhome-saas/ui-kit';
import {AuthStory} from '../components/auth-story';
import {AuthFacade} from '../facades/auth.facade';

@Component({
  selector: 'app-sign-in',
  imports: [AuthStory, RouterLink, TranslocoDirective],
  templateUrl: './sign-in.html',
  styleUrl: '../auth.css',
})
export class SignIn {
  protected readonly facade = inject(AuthFacade);
  protected readonly config = inject(UI_KIT_CONFIG);
}
