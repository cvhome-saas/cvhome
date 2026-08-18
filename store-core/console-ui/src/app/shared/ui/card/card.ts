import {Component} from '@angular/core';

@Component({
  selector: 'app-card',
  template: `<ng-content />`,
  host: {
    class: 'block rounded-2xl border border-border bg-muted p-5',
  },
})
export class Card {}
