import {Component} from '@angular/core';
import {NbLayoutModule} from '@nebular/theme';
import {FooterComponent} from '../../components/footer/footer.component';

@Component({
  selector: 'ngx-empty-layout',
  standalone: true,
  imports: [NbLayoutModule, FooterComponent],
  styleUrls: ['./empty.layout.scss'],
  template: `
    <nb-layout>

      <nb-layout-column>
        <ng-content select="router-outlet"></ng-content>
      </nb-layout-column>

      <nb-layout-footer fixed>
        <ngx-footer></ngx-footer>
      </nb-layout-footer>
    </nb-layout>
  `,
})
export class EmptyLayoutComponent {
}
