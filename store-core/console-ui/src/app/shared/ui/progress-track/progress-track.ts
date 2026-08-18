import {Component, input} from '@angular/core';

@Component({
  selector: 'app-progress-track',
  template: `<span [style.inline-size.%]="value()"></span>`,
  styles: `
    :host {
      display: block;
      overflow: hidden;
      block-size: 0.45rem;
      border-radius: var(--radius-full);
      background: var(--track);
    }

    span {
      display: block;
      block-size: 100%;
      border-radius: inherit;
      background: currentColor;
      transform-origin: left center;
    }
  `,
})
export class ProgressTrack {
  readonly value = input.required<number>();
}
