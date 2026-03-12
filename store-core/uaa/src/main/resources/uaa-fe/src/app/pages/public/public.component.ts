import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {HeaderComponent} from './componants/header/header.component';
import {FooterComponent} from './componants/footer/footer.component';

@Component({
  selector: 'app-public',
  template: `
    <div id="scrollUp" title="Scroll To Top">
      <i class="fas fa-arrow-up"></i>
    </div>
    <div class="main">
      <app-header/>
      <router-outlet/>
      <app-footer/>
    </div>`,
  imports: [
    RouterOutlet,
    HeaderComponent,
    FooterComponent
  ]
})
export class PublicComponent {
}
