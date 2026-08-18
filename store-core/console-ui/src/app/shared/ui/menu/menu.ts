import {Component} from '@angular/core';

@Component({
  selector: 'app-menu',
  template: `<ng-content />`,
  host: {
    class: 'block rounded-xl border border-border bg-card p-2 shadow-lift',
    role: 'menu',
  },
})
export class Menu {}
