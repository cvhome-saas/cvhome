import {Component, input} from '@angular/core';

@Component({
  selector: 'app-avatar',
  template: `{{ initials() }}`,
  host: {
    class: 'grid size-10 place-items-center rounded-full bg-chart-1-wash text-caption font-semibold text-chart-1-foreground',
  },
})
export class Avatar {
  readonly initials = input.required<string>();
}
