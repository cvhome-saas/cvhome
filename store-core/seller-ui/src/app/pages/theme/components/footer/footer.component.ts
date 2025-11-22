import {Component} from '@angular/core';

@Component({
  selector: 'ngx-footer',
  standalone: false,
  template: `
    <span>
      Created with ♥ by <b><a href="mailto:{{email}}">{{ email }}</a></b> {{ year }}
    </span>
  `,
})
export class FooterComponent {
  email: string = "me@asrevo.com"
  year: number = 2025
}
