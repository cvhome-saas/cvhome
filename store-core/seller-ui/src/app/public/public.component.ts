import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NbLayoutModule} from '@nebular/theme';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';

/**
 * The public shell.
 *
 * `nb-layout` is here for one reason: it is what hosts Nebular's overlay container. Without it
 * `NbToastrService` has nowhere to attach and public-area toasts silently do nothing — so a seller
 * registering an account would get no feedback at all, success or failure.
 */
@Component({
  selector: 'app-seller-ui-public',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, NbLayoutModule],
  styleUrls: ['public.component.scss'],
  template: `
    <nb-layout>
      <nb-layout-column class="p-0">
        <!--====== Scroll To Top Area Start ======-->
        <div id="scrollUp" title="Scroll To Top">
          <i class="fas fa-arrow-up"></i>
        </div>
        <!--====== Scroll To Top Area End ======-->

        <div class="main">
          <app-header/>
          <router-outlet/>
          <app-footer/>
        </div>
      </nb-layout-column>
    </nb-layout>
    `,
})
export class PublicComponent {

}
