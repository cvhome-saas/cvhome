import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';

@Component({
  selector: 'app-seller-ui-public',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  styleUrls: ['public.component.scss'],
  template: `
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
    `,
})
export class PublicComponent {

}
