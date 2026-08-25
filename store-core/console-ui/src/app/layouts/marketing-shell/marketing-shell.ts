import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-marketing-shell',
  imports: [RouterOutlet],
  template: `<router-outlet />`,
})
export class MarketingShell {}
