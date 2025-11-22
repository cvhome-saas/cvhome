import {Component} from '@angular/core';

@Component({
  selector: 'ngx-empty-layout',
  standalone: false,
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
