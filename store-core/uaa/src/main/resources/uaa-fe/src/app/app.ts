import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

import {ToastHost} from '@cvhome-saas/ui-kit/ui';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastHost],
  template: `
    <router-outlet />
    <app-toast-host />
  `,
})
export class App {}
