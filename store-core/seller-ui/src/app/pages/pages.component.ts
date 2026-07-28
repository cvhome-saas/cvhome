import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {PagesFacade} from './facades/pages.facade';

@Component({
  selector: 'ngx-pages',
  standalone: false,
  styleUrls: ['pages.component.scss'],
  template: `
    <ngx-one-column-layout>
      <nb-menu [items]="facade.menu()"></nb-menu>
      <router-outlet *ngIf="facade.storesReady()"></router-outlet>
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
