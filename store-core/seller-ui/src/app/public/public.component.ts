import {Component} from '@angular/core';

@Component({
  selector: 'app-seller-ui-public',
  standalone: false,
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
