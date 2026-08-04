import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {NbMenuModule} from '@nebular/theme';
import {OneColumnLayoutComponent} from './theme/layout/one-column/one-column.layout';
import {PagesFacade} from './facades/pages.facade';

@Component({
  selector: 'ngx-pages',
  standalone: true,
  imports: [OneColumnLayoutComponent, NbMenuModule, RouterOutlet],
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu [items]="facade.menu()"></nb-menu>
      @if (facade.storesReady()) {
        <router-outlet></router-outlet>
      }
    </ngx-one-column-layout>
  `,
  providers: [PagesFacade]
})
export class PagesComponent implements OnInit {
  protected readonly facade = inject(PagesFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
